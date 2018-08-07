package com.jaspercloud.txtransaction.service;

import com.jaspercloud.txtransaction.entity.TransactionData;

import java.util.List;

public interface TxTransactionStorageService {

    void saveCompensateTransaction(String serviceName, TransactionData transactionData);

    void saveCompensateTransaction(List<TransactionData> list);

    TransactionData getOneCompensateTransaction(String serviceName);
}
