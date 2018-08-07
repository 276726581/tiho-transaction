package com.jaspercloud.txtransaction.annotation;

import com.alipay.sofa.rpc.common.cache.ReflectCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

public class TxTransactionalServiceProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        String className = clazz.getName();
        Service service = (Service) clazz.getDeclaredAnnotation(Service.class);
        if (null == service) {
            return bean;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            TxTransactional annotation = method.getDeclaredAnnotation(TxTransactional.class);
            if (null == annotation) {
                continue;
            }
            ReflectCache.putClassCache(className, clazz);
            ReflectCache.putOverloadMethodCache(className, method);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
