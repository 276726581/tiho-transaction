package com.tiho.txtransaction.transport;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.tiho.txtransaction.component.TxClient;

public class RpcClientTransport implements RpcTransport {

    private TxClient txClient;

    public RpcClientTransport(TxClient txClient) {
        this.txClient = txClient;
    }

    @Override
    public SofaResponse invoke(SofaRequest sofaRequest) throws Exception {
        SofaResponse sofaResponse = txClient.invokeSync(sofaRequest);
        return sofaResponse;
    }
}
