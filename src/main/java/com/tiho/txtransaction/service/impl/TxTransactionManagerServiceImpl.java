package com.tiho.txtransaction.service.impl;

import com.alipay.remoting.Connection;
import com.alipay.sofa.rpc.common.struct.ConcurrentHashSet;
import com.alipay.sofa.rpc.event.Event;
import com.alipay.sofa.rpc.event.EventBus;
import com.alipay.sofa.rpc.event.Subscriber;
import com.tiho.txtransaction.entity.TransactionData;
import com.tiho.txtransaction.entity.TxGroup;
import com.tiho.txtransaction.event.OnServiceRegisteredEvent;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.service.TxTransactionStorageService;
import com.tiho.txtransaction.transport.RpcServerTransport;
import com.tiho.txtransaction.util.ConnectionTxIdUtil;
import com.tiho.txtransaction.util.ShortUUID;
import com.tiho.txtransaction.util.TxConnectionContext;
import com.tiho.txtransaction.util.TxConstants;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TxTransactionManagerServiceImpl implements TxTransactionManagerService, InitializingBean {

    private static final String Commit = "commit";
    private static final String Rollback = "rollback";

    private Logger logger = LoggerFactory.getLogger(TxTransactionManagerServiceImpl.class);

    private final Map<String, Set<Connection>> serviceMap = new ConcurrentHashMap<>();
    private final Map<String, TxGroup> txGroupMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    @Autowired
    private TxTransactionService txTransactionService;

    @Autowired
    private TxTransactionStorageService txTransactionStorageService;

    public TxTransactionManagerServiceImpl() {
    }

    @Override
    public void afterPropertiesSet() {
        if (null == txTransactionService) {
            throw new NullPointerException("txTransactionService is null");
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, TxGroup> entry : txGroupMap.entrySet()) {
                    try {
                        String txId = entry.getKey();
                        TxGroup txGroup = entry.getValue();
                        long timeout = txGroup.getTimeout();
                        if (-1 == timeout) {
                            continue;
                        }

                        long createTime = txGroup.getCreateTime();
                        long now = System.currentTimeMillis();
                        if ((now - createTime) > timeout) {
                            logger.debug("timeout: txId=" + txId);
                            rollbackTransactionGroup(txId);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                // TODO: 2018/7/16 事务补偿
                while (true) {
                    try {
                        Set<String> serviceSet = serviceMap.keySet();
                        for (String serviceName : serviceSet) {
                            Set<Connection> connections = serviceMap.get(serviceName);
                            if (connections.isEmpty()) {
                                continue;
                            }

                            TransactionData transactionData = txTransactionStorageService.getOneCompensateTransaction(serviceName);
                            if (null != transactionData) {
                                try {
                                    List<Connection> list = new ArrayList<>(connections);
                                    int size = connections.size();
                                    int rand = RandomUtils.nextInt(size);
                                    Connection connection = list.get(rand);
                                    TxConnectionContext.set(RpcServerTransport.ScopeName, connection);
                                    txTransactionService.compensateTransaction(transactionData);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                    txTransactionStorageService.saveCompensateTransaction(serviceName, transactionData);
                                }
                            }
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            logger.error(ex.getMessage());
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        EventBus.register(OnServiceRegisteredEvent.class, new Subscriber() {
            @Override
            public void onEvent(Event event) {
                OnServiceRegisteredEvent registeredEvent = (OnServiceRegisteredEvent) event;
                logger.debug("register: " + registeredEvent.getServiceName());
            }
        });
    }

    @Override
    public void registerService(String serviceName) {
        Connection connection = TxConnectionContext.get();
        Set<Connection> connections = serviceMap.get(serviceName);
        if (null == connections) {
            try {
                lock.lock();
                connections = serviceMap.get(serviceName);
                if (null == connections) {
                    connections = new ConcurrentHashSet<>();
                    serviceMap.put(serviceName, connections);
                }
            } finally {
                lock.unlock();
            }
        }
        connections.add(connection);
        EventBus.post(new OnServiceRegisteredEvent(serviceName));
    }

    public void unRegisterServiceConnection(Connection connection) {
        logger.debug("unRegisterServiceConnection");
        String serviceName = (String) connection.getAttribute(TxConstants.ServiceName);
        Set<Connection> connections = serviceMap.get(serviceName);
        connections.remove(connection);
        closeTransactionGroup(connection);
    }

    private void closeTransactionGroup(Connection connection) {
        List<String> txIdList = ConnectionTxIdUtil.getConnectionTxIdList(connection);
        for (String txId : txIdList) {
            try {
                rollbackTransactionGroup(txId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String createTransactionGroup(long timeout) {
        String txId = ShortUUID.generate();
        logger.debug("createTransactionGroup: txId=" + txId);
        TxGroup txGroup = new TxGroup(txId, timeout);
        txGroupMap.put(txId, txGroup);
        Connection connection = TxConnectionContext.get();
        ConnectionTxIdUtil.addConnectionTxId(connection, txId);
        txGroup.addConnection(connection);
        return txId;
    }

    @Override
    public void addTransactionGroup(String txId, TransactionData data) {
        TxGroup txGroup = txGroupMap.get(txId);
        if (null == txGroup) {
            logger.debug("miss addTransactionGroup txId=" + txId);
            return;
        }
        logger.debug("addTransactionGroup txId=" + txId);
        Connection connection = TxConnectionContext.get();
        ConnectionTxIdUtil.addConnectionTxId(connection, txId);
        txGroup.addConnection(connection);
        txGroup.addTransactionData(connection, data);
    }

    @Override
    public void commitTransactionGroup(String txId) {
        processTransactionGroup(txId, Commit);
    }

    @Override
    public void rollbackTransactionGroup(String txId) {
        processTransactionGroup(txId, Rollback);
    }

    private void processTransactionGroup(String txId, String type) {
        TxGroup txGroup = txGroupMap.remove(txId);
        if (null == txGroup) {
            logger.debug(String.format("miss %s txId=%s", type, txId));
            return;
        }
        logger.debug(String.format("%s txId=", type, txId));
        Set<Connection> connections = txGroup.getConnectionList();
        List<TransactionData> compensateList = new ArrayList<>();
        for (Connection connection : connections) {
            List<TransactionData> dataList = txGroup.getTransactionDataList(connection);
            if (!connection.isFine()) {
                // TODO: 2018/7/16 事务补偿
                compensateList.addAll(dataList);
                continue;
            }
            try {
                switch (type) {
                    case Commit: {
                        TxConnectionContext.set(RpcServerTransport.ScopeName, connection);
                        txTransactionService.commit(txId);
                        break;
                    }
                    case Rollback: {
                        TxConnectionContext.set(RpcServerTransport.ScopeName, connection);
                        txTransactionService.rollback(txId);
                        break;
                    }
                    default: {
                        logger.error("unSupport type: " + type);
                        break;
                    }
                }
                ConnectionTxIdUtil.removeConnectionTxId(connection, txId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // TODO: 2018/7/16 事务补偿
                compensateList.addAll(dataList);
            }
        }
        if (!compensateList.isEmpty()) {
            for (TransactionData data : compensateList) {
                String serviceName = data.getServiceName();
                txTransactionStorageService.saveCompensateTransaction(serviceName, data);
            }
        }
    }
}
