package com.tiho.txtransaction.entity;

import com.alipay.remoting.Connection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TxGroup {

    private String txId;
    private Set<Connection> connectionList = new HashSet<>();
    private Lock lock = new ReentrantLock();
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
            connection.addPoolKey(txId);
            connectionList.add(connection);
        } finally {
            lock.unlock();
        }
    }

    public void removeConnection(Connection connection) {
        try {
            lock.lock();
            connection.removePoolKey(txId);
            connectionList.remove(connection);
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

    @Override
    public String toString() {
        return "TxGroup{" +
                "txId='" + txId + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
