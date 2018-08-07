package com.jaspercloud.txtransaction.annotation;

import com.jaspercloud.txtransaction.config.TxTransactionClientConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(TxTransactionClientConfig.class)
public @interface EnableTxTransactionClient {
}
