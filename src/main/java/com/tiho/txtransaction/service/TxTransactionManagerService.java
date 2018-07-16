package com.tiho.txtransaction.service;

import com.tiho.txtransaction.entity.TransactionData;

public interface TxTransactionManagerService {

    void registerService(String serviceName);

    String createTransactionGroup(long timeout);

    void addTransactionGroup(String txId, TransactionData data);

    void commitTransactionGroup(String txId);

    void rollbackTransactionGroup(String txId);
}
