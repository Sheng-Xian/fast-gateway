package com.fast.gateway.client.core.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author sheng
 * @create 2023-07-22 22:42
 */
@Data
@ConfigurationProperties(prefix = FastProperties.FAST_PREFIX)
public class FastProperties {

    public static final String FAST_PREFIX = "fast";

    /**
     * ETCD registryAddress
     */
    private String registryAddress;

    /**
     * ETCD registry
     */
    private String namespace = FAST_PREFIX;

    /**
     * Environment properties
     */
    private String env = "dev";
}
