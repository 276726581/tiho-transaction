package com.tiho.txtransaction.annotation;

import com.tiho.txtransaction.config.TxTransactionClientConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(TxTransactionClientConfig.class)
public @interface EnableTxTransactionClient {
}
