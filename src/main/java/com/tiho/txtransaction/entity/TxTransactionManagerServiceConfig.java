package com.tiho.txtransaction.entity;

import com.alipay.sofa.rpc.event.Event;
import com.alipay.sofa.rpc.event.EventBus;
import com.alipay.sofa.rpc.event.Subscriber;
import com.tiho.txtransaction.component.TxClient;
import com.tiho.txtransaction.event.OnConnectionedEvent;
import com.tiho.txtransaction.proxy.ProxyInvokerUtil;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.service.impl.LocalTxTransactionService;
import com.tiho.txtransaction.transport.RpcClientTransport;

public class TxTransactionManagerServiceConfig {

    private String serviceName;
    private String host = "127.0.0.1";
    private int port = 80;
    private int timeout = 3 * 1000;
    private LocalTxTransactionService localTxTransactionService;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public LocalTxTransactionService getLocalTxTransactionService() {
        return localTxTransactionService;
    }

    public void setLocalTxTransactionService(LocalTxTransactionService localTxTransactionService) {
        this.localTxTransactionService = localTxTransactionService;
    }

    public TxTransactionManagerService export() {
        TxClient txClient = new TxClient(serviceName, host, port, timeout);
        txClient.setTxTransactionService(localTxTransactionService);
        txClient.init();
        RpcClientTransport rpcClientTransport = new RpcClientTransport(txClient);
        TxTransactionManagerService txTransactionManagerService = ProxyInvokerUtil.getInvoker(TxTransactionManagerService.class, rpcClientTransport);
        EventBus.register(OnConnectionedEvent.class, new Subscriber() {
            @Override
            public void onEvent(Event event) {
                txTransactionManagerService.registerService(serviceName);
            }
        });
        return txTransactionManagerService;
    }
}
