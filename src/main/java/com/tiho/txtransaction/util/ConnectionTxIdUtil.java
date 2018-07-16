package com.tiho.txtransaction.util;

import com.alipay.remoting.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ConnectionTxIdUtil {

    private static final String PREFIX = "TxId#";

    private ConnectionTxIdUtil() {
    }

    public static void addConnectionTxId(Connection connection, String txId) {
        connection.addPoolKey(PREFIX + txId);
    }

    public static void removeConnectionTxId(Connection connection, String txId) {
        connection.removePoolKey(PREFIX + txId);
    }

    public static List<String> getConnectionTxIdList(Connection connection) {
        Set<String> keys = connection.getPoolKeys();
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            if (key.startsWith(PREFIX)) {
                String txId = key.replaceAll(PREFIX, "");
                list.add(txId);
            }
        }
        return list;
    }
}
