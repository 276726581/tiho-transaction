package com.tiho.txtransaction.service.impl;

import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.support.TxProxyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalTxTransactionService implements TxTransactionService {

    private Logger logger = LoggerFactory.getLogger(LocalTxTransactionService.class);

    private final Map<String, List<TxProxyConnection>> txConnectionMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void addConnection(String txId, TxProxyConnection connection) {
        try {
            rwLock.writeLock().lock();

            List<TxProxyConnection> connectionList = txConnectionMap.get(txId);
            if (null == connectionList) {
                try {
                    lock.lock();
                    connectionList = txConnectionMap.get(txId);
                    if (null == connectionList) {
                        connectionList = new ArrayList<>();
                        txConnectionMap.put(txId, connectionList);
                    }
                } finally {
                    lock.unlock();
                }
            }
            connectionList.add(connection);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void commit(String txId) {
        List<TxProxyConnection> list = txConnectionMap.remove(txId);
        if (null == list) {
            return;
        }
        for (TxProxyConnection connection : list) {
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
        List<TxProxyConnection> list = txConnectionMap.remove(txId);
        if (null == list) {
            return;
        }
        for (TxProxyConnection connection : list) {
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
        Map<String, List<TxProxyConnection>> map = new HashMap<>();
        try {
            rwLock.readLock().lock();
            map.putAll(txConnectionMap);
            txConnectionMap.clear();
        } finally {
            rwLock.readLock().unlock();
        }
        for (List<TxProxyConnection> list : map.values()) {
            for (TxProxyConnection connection : list) {
                try {
                    connection.txClose();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
