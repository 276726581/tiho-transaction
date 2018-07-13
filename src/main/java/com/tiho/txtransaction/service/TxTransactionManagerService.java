package com.tiho.txtransaction.service;

public interface TxTransactionManagerService {

    String createTransactionGroup(long timeout);

    void addTransactionGroup(String txId);

    void commitTransactionGroup(String txId);

    void rollbackTransactionGroup(String txId);
}
