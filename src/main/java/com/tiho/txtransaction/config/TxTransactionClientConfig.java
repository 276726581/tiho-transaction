package com.tiho.txtransaction.config;

import com.tiho.txtransaction.annotation.TxTransactionalServiceScanner;
import com.tiho.txtransaction.entity.TxTransactionManagerServiceConfig;
import com.tiho.txtransaction.interceptor.feign.TransactionFeignInterceptor;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.service.impl.LocalTxTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan("com.tiho.txtransaction.aspect")
@Import(TxTransactionalServiceScanner.class)
@Configuration
public class TxTransactionClientConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${tx.manager.host}")
    private String host;

    @Value("${tx.manager.port}")
    private int port;

    @Value("${tx.manager.timeout}")
    private int timeout;

    @Bean
    public TransactionFeignInterceptor transactionFeignInterceptor() {
        return new TransactionFeignInterceptor();
    }

    @Bean
    public LocalTxTransactionService localTxTransactionService() {
        LocalTxTransactionService localTxTransactionService = new LocalTxTransactionService();
        return localTxTransactionService;
    }

    @Bean
    public TxTransactionManagerService txTransactionManagerService(LocalTxTransactionService localTxTransactionService) {
        TxTransactionManagerServiceConfig config = new TxTransactionManagerServiceConfig();
        config.setServiceName(appName);
        config.setHost(host);
        config.setPort(port);
        config.setTimeout(timeout);
        config.setLocalTxTransactionService(localTxTransactionService);
        TxTransactionManagerService txTransactionManagerService = config.export();
        return txTransactionManagerService;
    }
}
