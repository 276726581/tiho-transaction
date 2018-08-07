package com.jaspercloud.txtransaction.component;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.rpc.codec.bolt.SofaRpcSerializationRegister;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.event.EventBus;
import com.jaspercloud.txtransaction.event.OnConnectionedEvent;
import com.jaspercloud.txtransaction.proxy.RpcInvoker;
import com.jaspercloud.txtransaction.service.TxTransactionService;
import com.jaspercloud.txtransaction.service.impl.LocalTxTransactionService;
import com.jaspercloud.txtransaction.util.InvokeContextUtil;
import com.jaspercloud.txtransaction.util.TxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxClient {

    private Logger logger = LoggerFactory.getLogger(TxClient.class);

    private String serviceName;
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

    public TxClient(String serviceName, String host, int port, int timeout) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public void init() {
        if (null == serviceName) {
            throw new NullPointerException("serviceName is null");
        }
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
        rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT, new ConnectionEventProcessor() {
            @Override
            public void onEvent(String remoteAddr, Connection conn) {
                onConnection(remoteAddr, conn);
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        rpcClient.getConnection(getAddr(), timeout);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }
            }
        }).start();
    }

    public SofaResponse invokeSync(SofaRequest sofaRequest) throws Exception {
        InvokeContext invokeContext = InvokeContextUtil.createInvokeContext(sofaRequest);
        sofaRequest.addRequestProp(TxConstants.ServiceName, serviceName);
        SofaResponse sofaResponse = (SofaResponse) rpcClient.invokeSync(getAddr(), sofaRequest, invokeContext, timeout);
        return sofaResponse;
    }

    private void onRequest(BizContext bizCtx, AsyncContext asyncCtx, SofaRequest request) {
        SofaResponse response = rpcInvoker.invoke(request);
        asyncCtx.sendResponse(response);
    }

    private void onConnection(String remoteAddr, Connection conn) {
        EventBus.post(new OnConnectionedEvent());
    }

    private void onClose(String remoteAddr, Connection conn) {
        txTransactionService.clear();
    }

    private String getAddr() {
        String addr = host + ":" + port;
        return addr;
    }
}
