package com.fast.gateway.client;

import java.lang.annotation.*;

/**
 * A mandatory statement must be made on the service method
 * @author sheng
 * @create 2023-07-22 14:21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastInvoker {
    /**
     * access path
     * @return path
     */
    String path();
}
