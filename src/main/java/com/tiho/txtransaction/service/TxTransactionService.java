package com.tiho.txtransaction.service;

import com.tiho.txtransaction.entity.TransactionData;

public interface TxTransactionService {

    void compensateTransaction(TransactionData data) throws Exception;

    void commit(String txId);

    void rollback(String txId);
}
