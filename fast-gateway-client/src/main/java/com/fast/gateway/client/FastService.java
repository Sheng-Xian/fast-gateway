package com.fast.gateway.client;

import java.lang.annotation.*;

/**
 * Service definition annotation class
 * @author sheng
 * @create 2023-07-21 23:39
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastService {

    String serviceId();

    String version() default "1.0.0";

    FastProtocol protocol();

    /**
     * ANT path matching expression configuration
     * @return patternPath
     */
    String patternPath();
}
