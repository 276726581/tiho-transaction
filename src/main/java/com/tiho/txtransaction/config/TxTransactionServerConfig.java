package com.tiho.txtransaction.config;

import com.tiho.txtransaction.component.TxManagerServer;
import com.tiho.txtransaction.proxy.ProxyInvokerUtil;
import com.tiho.txtransaction.service.TxTransactionService;
import com.tiho.txtransaction.service.TxTransactionStorageService;
import com.tiho.txtransaction.service.impl.RedisTxTransactionStorageService;
import com.tiho.txtransaction.service.impl.TxTransactionManagerServiceImpl;
import com.tiho.txtransaction.support.redis.ProtobufRedisTemplate;
import com.tiho.txtransaction.transport.RpcServerTransport;
import com.tiho.txtransaction.transport.RpcTransport;
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
