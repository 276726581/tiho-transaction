package com.tiho.txtransaction.config;

import com.tiho.txtransaction.component.TxManagerServer;
import com.tiho.txtransaction.proxy.ProxyInvokerUtil;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.service.impl.TxTransactionManagerServiceImpl;
import com.tiho.txtransaction.transport.RpcServerTransport;
import com.tiho.txtransaction.transport.RpcTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TxTransactionServerConfig {

    @Value("${tx.manager.port}")
    private int port;

    @Value("${tx.invoke.timeout}")
    private int timeout;

    @Bean
    public TxTransactionService txTransactionService() {
        RpcTransport rpcTransport = new RpcServerTransport(timeout);
        TxTransactionService txTransactionService = ProxyInvokerUtil.getInvoker(TxTransactionService.class, rpcTransport);
        return txTransactionService;
    }

    @Bean
    public TxTransactionManagerService txTransactionManagerService(TxTransactionService txTransactionService) {
        TxTransactionManagerServiceImpl txTransactionManagerService = new TxTransactionManagerServiceImpl();
        txTransactionManagerService.setTxTransactionService(txTransactionService);
        return txTransactionManagerService;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TxManagerServer txManagerServer(TxTransactionManagerService txTransactionManagerService) {
        TxManagerServer txManagerServer = new TxManagerServer(port);
        txManagerServer.setTxTransactionManagerService(txTransactionManagerService);
        txManagerServer.init();
        return txManagerServer;
    }
}
