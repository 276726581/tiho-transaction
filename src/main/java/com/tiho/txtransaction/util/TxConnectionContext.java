package com.tiho.txtransaction.util;

import com.alipay.remoting.Connection;

public final class TxConnectionContext {

    private static final ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    private TxConnectionContext() {
    }

    public static void set(Connection connection) {
        threadLocal.set(connection);
    }

    public static Connection get() {
        Connection connection = threadLocal.get();
        return connection;
    }

    public static void remove() {
        threadLocal.remove();
    }
}
