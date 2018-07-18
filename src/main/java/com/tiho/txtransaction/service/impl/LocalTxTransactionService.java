package com.tiho.txtransaction.service.impl;

import com.alipay.sofa.rpc.common.cache.ReflectCache;
import com.alipay.sofa.rpc.common.struct.ConcurrentHashSet;
import com.tiho.txtransaction.entity.TransactionData;
import com.tiho.txtransaction.exception.NotFoundException;
import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.support.db.DbProxyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalTxTransactionService implements TxTransactionService {

    private Logger logger = LoggerFactory.getLogger(LocalTxTransactionService.class);

    private final Map<String, Set<DbProxyConnection>> txConnectionMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Autowired
    private BeanFactory beanFactory;

    public DbProxyConnection addConnection(String txId, Connection connection) {
        DbProxyConnection dbProxyConnection = new DbProxyConnection(connection);
        try {
            rwLock.writeLock().lock();
            Set<DbProxyConnection> connectionList = txConnectionMap.get(txId);
            if (null == connectionList) {
                try {
                    lock.lock();
                    connectionList = txConnectionMap.get(txId);
                    if (null == connectionList) {
                        connectionList = new ConcurrentHashSet<>();
                        txConnectionMap.put(txId, connectionList);
                    }
                } finally {
                    lock.unlock();
                }
            }
            connectionList.add(dbProxyConnection);
            return dbProxyConnection;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void compensateTransaction(TransactionData data) throws Exception {
        Class clazz = ReflectCache.getClassCache(data.getClassName());
        if (null == clazz) {
            throw new NotFoundException();
        }
        Method method = ReflectCache.getOverloadMethodCache(data.getClassName(), data.getMethodName(), data.getArgsType());
        if (null == method) {
            throw new NotFoundException();
        }
        Object bean = beanFactory.getBean(clazz);
        if (null == bean) {
            throw new NotFoundException();
        }
        method.invoke(bean, data.getArgs());
    }

    @Override
    public void commit(String txId) {
        Set<DbProxyConnection> list = txConnectionMap.remove(txId);
        if (null == list) {
            return;
        }
        for (DbProxyConnection connection : list) {
            try {
                connection.txCommit();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    connection.txClose();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public void rollback(String txId) {
        Set<DbProxyConnection> list = txConnectionMap.remove(txId);
        if (null == list) {
            return;
        }
        for (DbProxyConnection connection : list) {
            try {
                connection.txRollback();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    connection.txClose();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    public void clear() {
        Map<String, Set<DbProxyConnection>> map = new HashMap<>();
        try {
            rwLock.readLock().lock();
            map.putAll(txConnectionMap);
            txConnectionMap.clear();
        } finally {
            rwLock.readLock().unlock();
        }
        for (Set<DbProxyConnection> list : map.values()) {
            for (DbProxyConnection connection : list) {
                try {
                    connection.txClose();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
