package com.jaspercloud.txtransaction.service;

import com.jaspercloud.txtransaction.entity.TransactionData;

public interface TxTransactionService {

    void compensateTransaction(TransactionData data) throws Exception;

    void commit(String txId);

    void rollback(String txId);
}
