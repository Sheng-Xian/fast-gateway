package com.fast.gateway.client.core;

import com.fast.gateway.client.core.autoconfigure.FastProperties;
import com.fast.gateway.common.config.Service;
import com.fast.gateway.common.config.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Abstract registry manager
 * @author sheng
 * @create 2023-07-23 0:14
 */
@Slf4j
public abstract class AbstractClientRegistryManager {

    public static final String PROPERTIES_PATH = "fast.properties";

    public static final String REGISTRY_ADDRESS_KEY = "registryAddress";

    public static final String NAMESPACE_KEY = "namespace";

    public static final String ENV_KEY = "env";

    protected volatile boolean whetherStart = false;

    public static Properties properties = new Properties();

    public static String registryAddress;

    public static String namespace;

    public static String env;

    // TODO: registry center path const

    // Static code block load fast.properties file
    static  {
        InputStream is = null;
        is = AbstractClientRegistryManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
        try {
            if (is != null) {
                properties.load(is);
                registryAddress = properties.getProperty(REGISTRY_ADDRESS_KEY);
                namespace = properties.getProperty(NAMESPACE_KEY);
                env = properties.getProperty(ENV_KEY);
                if (StringUtils.isBlank(registryAddress)) {
                    String errorMessage = "Fast Gateway registry address can't be blank";
                    log.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
                if (StringUtils.isBlank(namespace)) {
                    namespace = FastProperties.FAST_PREFIX;
                }
            }
        } catch (Exception e) {
            log.error("#AbstractClientRegistryManager# Load InputStream hit error", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                    //ignore
                    log.error("#AbstractClientRegistryManager# InputStream close hit error", ex);
                }
            }

        }
    }

    // application.properties / yml has the highest priority
    protected AbstractClientRegistryManager(FastProperties fastProperties) {
        // 1. Initialize loading configuration information
        if (fastProperties.getRegistryAddress() != null) {
            registryAddress = fastProperties.getRegistryAddress();
            namespace = fastProperties.getNamespace();
            if (StringUtils.isBlank(namespace)) {
                namespace = FastProperties.FAST_PREFIX;
            }
            env = fastProperties.getEnv();
        }
        // TODO: Initialize and load the registry center object
    }

    // Build directory tree

    /**
     * Registry top-level structure directory path, only need to build once
     * @param path - String
     * @throws Exception - Exception
     */
    private void generatorStructPath(String path) throws Exception {

    }

    /**
     * Register service
     * @param service - Service
     * @throws Exception - exception
     */
    protected void registerService(Service service) throws Exception {

    }

    /**
     *
     * @param serviceInstance - ServiceInstance
     */
    protected void registerServiceInstance(ServiceInstance serviceInstance) {

    }

    public static String getRegistryAddress() {
        return registryAddress;
    }

    public static String getNamespace() {
        return namespace;
    }

    public static String getEnv() {
        return env;
    }
}
