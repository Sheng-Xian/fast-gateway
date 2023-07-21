package com.fast.gateway.core.netty.processor.filter;

import java.lang.annotation.*;

/**
 * @author sheng
 * @create 2023-07-16 13:17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter  {
    // Mandatory, unique id
    String id();

    String name() default "";

    ProcessorFilterType value() default ProcessorFilterType.PRE;

    // Order to sort filters, execute filters according to order from smallest to highest
    int order() default 0;
}
