package com.tiho.txtransaction.aspect;

import com.tiho.txtransaction.service.impl.LocalTxTransactionService;
import com.tiho.txtransaction.support.db.DbProxyConnection;
import com.tiho.txtransaction.util.TxTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Aspect
@Component
public class DataSourceAspect implements InitializingBean, Ordered {

    private Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    @Autowired
    private LocalTxTransactionService localTxTransactionService;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug("DataSourceAspect init");
    }

    @Around("execution(* javax.sql.DataSource.getConnection(..))")
    public Connection aroundGetConnection(ProceedingJoinPoint point) throws Throwable {
        Connection connection = (Connection) point.proceed();
        String txId = TxTransactionContext.current().getTxId();
        if (null == txId) {
            return connection;
        }

        DbProxyConnection dbProxyConnection = localTxTransactionService.addConnection(txId, connection);
        return dbProxyConnection;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
