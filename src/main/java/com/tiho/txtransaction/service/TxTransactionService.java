package com.tiho.txtransaction.service;

public interface TxTransactionService {

    void commit(String txId);

    void rollback(String txId);
}
