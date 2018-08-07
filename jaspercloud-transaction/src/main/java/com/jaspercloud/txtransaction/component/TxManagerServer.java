package com.jaspercloud.txtransaction.component;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.rpc.codec.bolt.SofaRpcSerializationRegister;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.jaspercloud.txtransaction.proxy.RpcInvoker;
import com.jaspercloud.txtransaction.service.TxTransactionManagerService;
import com.jaspercloud.txtransaction.service.impl.TxTransactionManagerServiceImpl;
import com.jaspercloud.txtransaction.util.InvokeContextUtil;
import com.jaspercloud.txtransaction.util.TxConnectionContext;
import com.jaspercloud.txtransaction.util.TxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxManagerServer {

    private Logger logger = LoggerFactory.getLogger(TxManagerServer.class);

    private int port;
    private static RpcServer rpcServer;

    private TxTransactionManagerServiceImpl txTransactionManagerService;
    private RpcInvoker<TxTransactionManagerService> rpcInvoker;

    static {
        SofaRpcSerializationRegister.registerCustomSerializer();
    }

    public void setTxTransactionManagerServiceImpl(TxTransactionManagerServiceImpl txTransactionManagerService) {
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
            Connection connection = bizCtx.getConnection();
            TxConnectionContext.set(connection);
            setRpcInvokeContext(bizCtx, asyncCtx, request);
            SofaResponse response = rpcInvoker.invoke(request);
            asyncCtx.sendResponse(response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            TxConnectionContext.remove();
        }
    }

    private void setRpcInvokeContext(BizContext bizCtx, AsyncContext asyncCtx, SofaRequest request) {
        Connection connection = bizCtx.getConnection();
        String serviceName = (String) request.getRequestProp(TxConstants.ServiceName);
        connection.setAttribute(TxConstants.ServiceName, serviceName);
    }

    private void onClose(String remoteAddr, Connection connection) {
        try {
            TxConnectionContext.set(connection);
            txTransactionManagerService.unRegisterServiceConnection(connection);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            TxConnectionContext.remove();
        }
    }
}
