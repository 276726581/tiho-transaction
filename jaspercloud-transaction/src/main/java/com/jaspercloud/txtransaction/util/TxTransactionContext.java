package com.jaspercloud.txtransaction.util;

public final class TxTransactionContext {

    public static final String TxId = "TxId";

    private static InheritableThreadLocal<TxTransactionContext> threadLocal = new InheritableThreadLocal<TxTransactionContext>() {
        @Override
        protected TxTransactionContext initialValue() {
            return new TxTransactionContext();
        }
    };

    private String txId;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    private TxTransactionContext() {
    }

    public static TxTransactionContext current() {
        TxTransactionContext context = threadLocal.get();
        return context;
    }

    public static void remove() {
        threadLocal.remove();
    }
}
