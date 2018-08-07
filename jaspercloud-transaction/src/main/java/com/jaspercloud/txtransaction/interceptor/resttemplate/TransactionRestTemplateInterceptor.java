package com.jaspercloud.txtransaction.interceptor.resttemplate;

import com.jaspercloud.txtransaction.util.TxTransactionContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import java.io.IOException;

public class TransactionRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution execution) throws IOException {
        String txId = TxTransactionContext.current().getTxId();
        if (!StringUtils.isEmpty(txId)) {
            httpRequest.getHeaders().add(TxTransactionContext.TxId, txId);
        }
        ClientHttpResponse response = execution.execute(httpRequest, bytes);
        return response;
    }
}
