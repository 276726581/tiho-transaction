package com.jaspercloud.txtransaction.util;

import com.alipay.remoting.Connection;

import java.util.HashMap;
import java.util.Map;

public final class TxConnectionContext {

    public static final String DEFAULT = "default";
    private static final InheritableThreadLocal<Map<String, Connection>> threadLocal = new InheritableThreadLocal<Map<String, Connection>>() {
        @Override
        protected Map<String, Connection> initialValue() {
            return new HashMap<>();
        }
    };

    private TxConnectionContext() {
    }

    public static void set(Connection connection) {
        Map<String, Connection> map = threadLocal.get();
        map.put(DEFAULT, connection);
    }

    public static void set(String scope, Connection connection) {
        Map<String, Connection> map = threadLocal.get();
        map.put(scope, connection);
    }

    public static Connection get(String scope) {
        Map<String, Connection> map = threadLocal.get();
        Connection connection = map.get(scope);
        return connection;
    }

    public static Connection get() {
        Map<String, Connection> map = threadLocal.get();
        Connection connection = map.get(DEFAULT);
        return connection;
    }

    public static void remove() {
        threadLocal.remove();
    }
}
