package com.tiho.txtransaction.service;

import com.tiho.txtransaction.entity.TransactionData;

public interface TxTransactionStorageService {

    void saveCompensateTransaction(String serviceName, TransactionData transactionData);

    TransactionData getOneCompensateTransaction(String serviceName);
}
