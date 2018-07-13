package com.tiho.txtransaction.util;

public final class TxContext {

    public static final String TxId = "TxId";

    private static InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<String>();

    private TxContext() {
    }

    public static String current() {
        String txId = threadLocal.get();
        return txId;
    }

    public static void set(String txId) {
        threadLocal.set(txId);
    }

    public static void remove() {
        threadLocal.remove();
    }
}
