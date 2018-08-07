package com.jaspercloud.txtransaction.interceptor.feign;

import com.jaspercloud.txtransaction.util.TxTransactionContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

public class TransactionFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String txId = TxTransactionContext.current().getTxId();
        if (!StringUtils.isEmpty(txId)) {
            requestTemplate.header(TxTransactionContext.TxId, txId);
        }
    }
}
