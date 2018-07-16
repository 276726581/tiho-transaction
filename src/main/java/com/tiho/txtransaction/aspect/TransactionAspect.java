package com.tiho.txtransaction.aspect;

import com.alipay.sofa.rpc.common.utils.ClassTypeUtils;
import com.tiho.txtransaction.annotation.TxTransactional;
import com.tiho.txtransaction.entity.TransactionData;
import com.tiho.txtransaction.service.TxTransactionManagerService;
import com.tiho.txtransaction.util.TxTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public void afterPropertiesSet() {
        logger.debug("TransactionAspect init");
    }

    @Around("@annotation(com.tiho.txtransaction.annotation.TxTransactional)")
    public Object aroundTransactional(ProceedingJoinPoint point) throws Throwable {
        Object target = point.getTarget();
        String className = target.getClass().getName();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        Object[] args = point.getArgs();
        Class[] argsTypes = method.getParameterTypes();
        String[] types = ClassTypeUtils.getTypeStrs(argsTypes, true);
        TxTransactional transactional = method.getAnnotation(TxTransactional.class);

        HttpServletRequest request = getHttpServletRequest();
        if (null == request) {
            Object result = point.proceed();
            return result;
        }

        String txId = request.getHeader(TxTransactionContext.TxId);
        if (null == txId) {
            int timeout = transactional.timeout();
            txId = txTransactionManagerService.createTransactionGroup(timeout);
            try {
                TxTransactionContext.current().setTxId(txId);
                logger.debug("TxTransaction begin");
                Object result = point.proceed();
                logger.debug("TxTransaction commit");
                txTransactionManagerService.commitTransactionGroup(txId);
                return result;
            } catch (Throwable e) {
                logger.debug("TxTransaction rollback");
                txTransactionManagerService.rollbackTransactionGroup(txId);
                throw e;
            } finally {
                TxTransactionContext.remove();
            }
        } else {
            try {
                TxTransactionContext.current().setTxId(txId);
                logger.debug("TxTransaction join");
                Object result = point.proceed();
                logger.debug("TxTransaction waiting");
                TransactionData transactionData = new TransactionData(txId, appName, className, methodName, args, types);
                txTransactionManagerService.addTransactionGroup(txId, transactionData);
                return result;
            } finally {
                TxTransactionContext.remove();
            }
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null == servletRequestAttributes) {
            return null;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
