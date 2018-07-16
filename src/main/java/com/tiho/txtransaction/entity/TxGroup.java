package com.tiho.txtransaction.entity;

import com.alipay.remoting.Connection;
import com.alipay.sofa.rpc.common.struct.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TxGroup {

    private transient Lock lock = new ReentrantLock();
    private String txId;
    private Set<Connection> connectionList = new ConcurrentHashSet<>();
    private List<TransactionData> dataList = new CopyOnWriteArrayList<>();
    private Map<String, List<TransactionData>> clientDataList = new ConcurrentHashMap<>();
    private long createTime = System.currentTimeMillis();
    private long timeout;

    public TxGroup(String txId, long timeout) {
        this.txId = txId;
        this.timeout = timeout;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void addConnection(Connection connection) {
        try {
            lock.lock();
            connectionList.add(connection);
        } finally {
            lock.unlock();
        }
    }

    public Set<Connection> getConnectionList() {
        try {
            lock.lock();
            return connectionList;
        } finally {
            lock.unlock();
        }
    }

    public void addTransactionData(Connection connection, TransactionData data) {
        try {
            lock.lock();
            dataList.add(data);

            String key = connection.getRemoteAddress().toString();
            List<TransactionData> list = clientDataList.get(key);
            if (null == list) {
                list = new CopyOnWriteArrayList<>();
                clientDataList.put(key, list);
            }
            list.add(data);
        } finally {
            lock.unlock();
        }
    }

    public List<TransactionData> getTransactionDataList(Connection connection) {
        try {
            lock.lock();
            String key = connection.getRemoteAddress().toString();
            List<TransactionData> list = clientDataList.get(key);
            if (null == list) {
                return new ArrayList<>();
            }
            return list;
        } finally {
            lock.unlock();
        }
    }

    public List<TransactionData> getTransactionDataList() {
        try {
            lock.lock();
            return dataList;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "TxGroup{" +
                "txId='" + txId + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
