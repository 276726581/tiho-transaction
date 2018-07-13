package com.tiho.txtransaction.interceptor.feign;

import com.tiho.txtransaction.util.TxContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

public class TransactionFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String txId = TxContext.current();
        if (!StringUtils.isEmpty(txId)) {
            requestTemplate.header(TxContext.TxId, txId);
        }
    }
}
