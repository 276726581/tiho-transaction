package com.jaspercloud.txtransaction.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TxTransactional {

    int timeout() default -1;

//    /**
//     * 回滚异常
//     *
//     * @return
//     */
//    Class<? extends Throwable>[] rollbackFor() default {};
//
//
//    /**
//     * 不回滚异常
//     *
//     * @return
//     */
//    Class<? extends Throwable>[] noRollbackFor() default {};
}
