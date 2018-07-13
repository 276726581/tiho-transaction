package com.tiho.txtransaction.component;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.rpc.codec.bolt.SofaRpcSerializationRegister;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.tiho.txtransaction.proxy.RpcInvoker;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.util.InvokeContextUtil;
import com.tiho.txtransaction.util.TxConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class TxManagerServer {

    private Logger logger = LoggerFactory.getLogger(TxManagerServer.class);

    private int port;
    private static RpcServer rpcServer;

    private TxTransactionManagerService txTransactionManagerService;
    private RpcInvoker<TxTransactionManagerService> rpcInvoker;

    static {
        SofaRpcSerializationRegister.registerCustomSerializer();
    }

    public void setTxTransactionManagerService(TxTransactionManagerService txTransactionManagerService) {
        this.txTransactionManagerService = txTransactionManagerService;
    }

    public TxManagerServer(int port) {
        this.port = port;
    }

    public void init() {
        if (null == txTransactionManagerService) {
            throw new NullPointerException("txTransactionManagerService is null");
        }

        rpcServer = new RpcServer(port);
        rpcServer.init();
        rpcServer.registerUserProcessor(new AsyncUserProcessor<SofaRequest>() {
            @Override
            public String interest() {
                return SofaRequest.class.getName();
            }

            @Override
            public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, SofaRequest request) {
                onRequest(bizCtx, asyncCtx, request);
            }
        });
        rpcServer.addConnectionEventProcessor(ConnectionEventType.CLOSE, new ConnectionEventProcessor() {
            @Override
            public void onEvent(String remoteAddr, Connection conn) {
                onClose(remoteAddr, conn);
            }
        });

        rpcInvoker = new RpcInvoker<>(TxTransactionManagerService.class, txTransactionManagerService);
    }

    public static SofaResponse invokeSync(Connection connection, SofaRequest sofaRequest, int timeout) throws Exception {
        if (null == rpcServer) {
            throw new NullPointerException("rpcServer is null");
        }
        InvokeContext invokeContext = InvokeContextUtil.createInvokeContext(sofaRequest);
        SofaResponse sofaResponse = (SofaResponse) rpcServer.invokeSync(connection, sofaRequest, invokeContext, timeout);
        return sofaResponse;
    }

    public void start() {
        rpcServer.start();
    }

    public void stop() {
        rpcServer.stop();
    }

    private void onRequest(BizContext bizCtx, AsyncContext asyncCtx, SofaRequest request) {
        try {
            try {
                Connection connection = bizCtx.getConnection();
                TxConnectionContext.set(connection);
                SofaResponse response = rpcInvoker.invoke(request);
                asyncCtx.sendResponse(response);
            } finally {
                TxConnectionContext.remove();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void onClose(String remoteAddr, Connection conn) {
        Set<String> txIdSet = conn.getPoolKeys();
        for (String txId : txIdSet) {
            try {
                rpcInvoker.getRef().rollbackTransactionGroup(txId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
