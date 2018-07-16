package com.tiho.txtransaction.annotation;

import com.alipay.sofa.rpc.common.cache.ReflectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Set;

public class TxTransactionalServiceScanner implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(TxTransactionalServiceScanner.class);

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String[] paths = environment.getProperty("tx.service.scan", new String[]{}.getClass());
        if (null == paths) {
            throw new NullPointerException("tx.service.scan not value");
        }
        ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry, true);
        classPathBeanDefinitionScanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        for (String path : paths) {
            Set<BeanDefinition> definitions = classPathBeanDefinitionScanner.findCandidateComponents(path);
            for (BeanDefinition definition : definitions) {
                try {
                    Class clazz = Class.forName(definition.getBeanClassName());
                    String className = clazz.getName();
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        TxTransactional annotation = method.getAnnotation(TxTransactional.class);
                        if (null == annotation) {
                            continue;
                        }
                        ReflectCache.putClassCache(className, clazz);
                        ReflectCache.putOverloadMethodCache(className, method);
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
