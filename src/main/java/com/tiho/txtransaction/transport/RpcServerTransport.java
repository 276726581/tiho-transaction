package com.tiho.txtransaction.transport;

import com.alipay.remoting.Connection;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.tiho.txtransaction.component.TxManagerServer;
import com.tiho.txtransaction.util.TxConnectionContext;

public class RpcServerTransport implements RpcTransport {

    public static final String ScopeName = "sendClient";

    private int timeout;

    public RpcServerTransport(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public SofaResponse invoke(SofaRequest sofaRequest) throws Exception {
        Connection connection = TxConnectionContext.get(ScopeName);
        SofaResponse sofaResponse = TxManagerServer.invokeSync(connection, sofaRequest, timeout);
        return sofaResponse;
    }
}
