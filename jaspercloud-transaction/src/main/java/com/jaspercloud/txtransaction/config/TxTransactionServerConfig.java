package com.jaspercloud.txtransaction.config;

import com.jaspercloud.txtransaction.component.TxManagerServer;
import com.jaspercloud.txtransaction.proxy.ProxyInvokerUtil;
import com.jaspercloud.txtransaction.service.TxTransactionService;
import com.jaspercloud.txtransaction.service.TxTransactionStorageService;
import com.jaspercloud.txtransaction.service.impl.RedisTxTransactionStorageService;
import com.jaspercloud.txtransaction.service.impl.TxTransactionManagerServiceImpl;
import com.jaspercloud.txtransaction.support.redis.ProtobufRedisTemplate;
import com.jaspercloud.txtransaction.transport.RpcServerTransport;
import com.jaspercloud.txtransaction.transport.RpcTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

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
    public TxTransactionManagerServiceImpl txTransactionManagerServiceImpl() {
        TxTransactionManagerServiceImpl txTransactionManagerService = new TxTransactionManagerServiceImpl();
        return txTransactionManagerService;
    }

    @ConditionalOnMissingBean(ProtobufRedisTemplate.class)
    @Bean
    public ProtobufRedisTemplate protobufRedisTemplate(RedisConnectionFactory connectionFactory) {
        ProtobufRedisTemplate protobufRedisTemplate = new ProtobufRedisTemplate(connectionFactory);
        return protobufRedisTemplate;
    }

    @ConditionalOnMissingBean(TxTransactionStorageService.class)
    @Bean
    public TxTransactionStorageService txTransactionStorageService() {
        TxTransactionStorageService txTransactionStorageService = new RedisTxTransactionStorageService();
        return txTransactionStorageService;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TxManagerServer txManagerServer(TxTransactionManagerServiceImpl txTransactionManagerServiceImpl) {
        TxManagerServer txManagerServer = new TxManagerServer(port);
        txManagerServer.setTxTransactionManagerServiceImpl(txTransactionManagerServiceImpl);
        txManagerServer.init();
        return txManagerServer;
    }
}
