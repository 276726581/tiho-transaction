package com.tiho.txtransaction.interceptor.resttemplate;

import com.tiho.txtransaction.util.TxContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import java.io.IOException;

public class TransactionRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution execution) throws IOException {
        String txId = TxContext.current();
        if (!StringUtils.isEmpty(txId)) {
            httpRequest.getHeaders().add(TxContext.TxId, txId);
        }
        ClientHttpResponse response = execution.execute(httpRequest, bytes);
        return response;
    }
}
