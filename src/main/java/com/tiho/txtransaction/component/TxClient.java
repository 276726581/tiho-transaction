package com.tiho.txtransaction.component;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.rpc.codec.bolt.SofaRpcSerializationRegister;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.tiho.txtransaction.proxy.RpcInvoker;
import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.service.impl.LocalTxTransactionService;
import com.tiho.txtransaction.util.InvokeContextUtil;

public class TxClient {

    private String host;
    private int port;
    private int timeout;
    private RpcClient rpcClient;

    private LocalTxTransactionService txTransactionService;
    private RpcInvoker<TxTransactionService> rpcInvoker;

    static {
        SofaRpcSerializationRegister.registerCustomSerializer();
    }

    public void setTxTransactionService(LocalTxTransactionService txTransactionService) {
        this.txTransactionService = txTransactionService;
    }

    public TxClient(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public void init() {
        rpcClient = new RpcClient();
        rpcClient.init();
        rpcClient.registerUserProcessor(new AsyncUserProcessor<SofaRequest>() {
            @Override
            public String interest() {
                return SofaRequest.class.getName();
            }

            @Override
            public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, SofaRequest request) {
                onRequest(bizCtx, asyncCtx, request);
            }
        });
        rpcClient.addConnectionEventProcessor(ConnectionEventType.CLOSE, new ConnectionEventProcessor() {
            @Override
            public void onEvent(String remoteAddr, Connection conn) {
                onClose(remoteAddr, conn);
            }
        });

        if (null == txTransactionService) {
            throw new NullPointerException("txTransactionService is null");
        }
        rpcInvoker = new RpcInvoker(TxTransactionService.class, txTransactionService);
    }

    public SofaResponse invokeSync(SofaRequest sofaRequest) throws Exception {
        InvokeContext invokeContext = InvokeContextUtil.createInvokeContext(sofaRequest);
        SofaResponse sofaResponse = (SofaResponse) rpcClient.invokeSync(getAddr(), sofaRequest, invokeContext, timeout);
        return sofaResponse;
    }

    private void onRequest(BizContext bizCtx, AsyncContext asyncCtx, SofaRequest request) {
        SofaResponse response = rpcInvoker.invoke(request);
        asyncCtx.sendResponse(response);
    }

    private void onClose(String remoteAddr, Connection conn) {
        txTransactionService.clear();
    }

    private String getAddr() {
        String addr = host + ":" + port;
        return addr;
    }
}
