package com.tiho.txtransaction.service.impl;

import com.alipay.remoting.Connection;
import com.tiho.txtransaction.entity.TxGroup;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.util.ShortUUID;
import com.tiho.txtransaction.util.TxConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TxTransactionManagerServiceImpl implements TxTransactionManagerService, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(TxTransactionManagerServiceImpl.class);

    private Map<String, TxGroup> txGroupMap = new ConcurrentHashMap<>();

    private TxTransactionService txTransactionService;

    public void setTxTransactionService(TxTransactionService txTransactionService) {
        this.txTransactionService = txTransactionService;
    }

    public TxTransactionManagerServiceImpl() {
    }

    @Override
    public void afterPropertiesSet() {
        if (null == txTransactionService) {
            throw new NullPointerException("txTransactionService is null");
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
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
    }

    @Override
    public String createTransactionGroup(long timeout) {
        String txId = ShortUUID.generate();
        logger.debug("createTransactionGroup: txId=" + txId);
        TxGroup txGroup = new TxGroup(txId, timeout);
        txGroupMap.put(txId, txGroup);
        Connection connection = TxConnectionContext.get();
        txGroup.addConnection(connection);
        return txId;
    }

    @Override
    public void addTransactionGroup(String txId) {
        TxGroup txGroup = txGroupMap.get(txId);
        if (null == txGroup) {
            logger.debug("miss addTransactionGroup txId=" + txId);
            return;
        }
        logger.debug("addTransactionGroup txId=" + txId);
        Connection connection = TxConnectionContext.get();
        txGroup.addConnection(connection);
    }

    @Override
    public void commitTransactionGroup(String txId) {
        TxGroup txGroup = txGroupMap.remove(txId);
        if (null == txGroup) {
            logger.debug("miss commit txId=" + txId);
            return;
        }
        logger.debug("commit txId=" + txId);
        Set<Connection> connectionSet = txGroup.getConnectionList();
        List<Connection> connectionList = new ArrayList<>(connectionSet);
        for (Connection connection : connectionList) {
            try {
                TxConnectionContext.set(connection);
                txTransactionService.commit(txId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                TxConnectionContext.remove();
            }
        }
    }

    @Override
    public void rollbackTransactionGroup(String txId) {
        TxGroup txGroup = txGroupMap.remove(txId);
        if (null == txGroup) {
            logger.debug("miss rollback txId=" + txId);
            return;
        }
        logger.debug("rollback txId=" + txId);
        Set<Connection> connectionSet = txGroup.getConnectionList();
        List<Connection> connectionList = new ArrayList<>(connectionSet);
        for (Connection connection : connectionList) {
            try {
                TxConnectionContext.set(connection);
                txTransactionService.rollback(txId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                TxConnectionContext.remove();
            }
        }
    }
}
