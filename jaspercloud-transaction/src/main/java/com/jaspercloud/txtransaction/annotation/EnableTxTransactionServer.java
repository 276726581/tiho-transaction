package com.jaspercloud.txtransaction.annotation;

import com.jaspercloud.txtransaction.config.TxTransactionServerConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(TxTransactionServerConfig.class)
public @interface EnableTxTransactionServer {
}
