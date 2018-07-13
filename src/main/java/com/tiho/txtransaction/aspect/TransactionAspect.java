package com.tiho.txtransaction.aspect;

import com.tiho.txtransaction.annotation.TxTransactional;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.util.TxContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class TransactionAspect implements InitializingBean, Ordered {

    private Logger logger = LoggerFactory.getLogger(TransactionAspect.class);

    @Autowired
    private TxTransactionManagerService txTransactionManagerService;

    @Override
    public void afterPropertiesSet() {
        logger.debug("TransactionAspect init");
    }

    @Around("@annotation(com.tiho.txtransaction.annotation.TxTransactional)")
    public Object aroundTransactional(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        TxTransactional transactional = method.getAnnotation(TxTransactional.class);

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String txId = request.getHeader(TxContext.TxId);
        boolean head;
        if (null == txId) {
            int timeout = transactional.timeout();
            txId = txTransactionManagerService.createTransactionGroup(timeout);
            head = true;
        } else {
            txTransactionManagerService.addTransactionGroup(txId);
            head = false;
        }
        try {
            TxContext.set(txId);
            logger.debug("TxTransaction begin");
            Object result = point.proceed();
            if (head) {
                logger.debug("TxTransaction send commit");
                txTransactionManagerService.commitTransactionGroup(txId);
            } else {
                logger.debug("TxTransaction waiting commit");
            }
            return result;
        } catch (Throwable e) {
            if (head) {
                logger.debug("TxTransaction send rollback");
                txTransactionManagerService.rollbackTransactionGroup(txId);
            } else {
                logger.debug("TxTransaction waiting rollback");
            }
            throw e;
        } finally {
            TxContext.remove();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
