package com.jaspercloud.txtransaction.config;

import com.jaspercloud.txtransaction.annotation.TxTransactionalServiceProcessor;
import com.jaspercloud.txtransaction.entity.TxTransactionManagerServiceConfig;
import com.jaspercloud.txtransaction.interceptor.feign.TransactionFeignInterceptor;
import com.jaspercloud.txtransaction.service.TxTransactionManagerService;
import com.jaspercloud.txtransaction.service.impl.LocalTxTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("com.jaspercloud.txtransaction.aspect")
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
    public TxTransactionalServiceProcessor txTransactionalServiceProcessor() {
        return new TxTransactionalServiceProcessor();
    }

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
