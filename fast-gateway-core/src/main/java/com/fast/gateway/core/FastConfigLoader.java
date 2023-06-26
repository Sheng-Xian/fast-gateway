package com.fast.gateway.core;

import com.fast.gateway.common.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Gateway config loading rule:
 * runtime parameters > jvm parameters > environment variables > config file > internal fastConfig object default attributes value
 * @author sheng
 * @create 2023-06-18 22:25
 */
@Slf4j
public class FastConfigLoader {
    private final static String CONFIG_FILE = "fast.properties";
    private final static String CONFIG_ENV_PREFIX = "FAST_";
    private final static String CONFIG_JVM_PREFIX = "fast.";
    private FastConfigLoader() {

    }
    private final static FastConfigLoader INSTANCE = new FastConfigLoader();

    public static FastConfigLoader getInstance() {
        return  INSTANCE;
    }

    private FastConfig fastConfig = new FastConfig();

    public static FastConfig getFastConfig() {
        return INSTANCE.fastConfig;
    }

    public FastConfig load(String args[]) {
        //1. load config file
        {
            InputStream is = FastConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                Properties properties = new Properties();
                try {
                    properties.load(is);
                    PropertiesUtils.properties2Object(properties, fastConfig);
                } catch (IOException e) {
                    log.warn("FastConfigLoader# load config file {} failed.", CONFIG_FILE, e);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //ignore the error
                    }
                }
            }
        }
        // 2. load environment variables
        {
            Map<String, String> env = System.getenv();
            Properties properties = new Properties();
            properties.putAll(env);
            PropertiesUtils.properties2Object(properties, fastConfig, CONFIG_ENV_PREFIX);

        }
        // 3. load JVM parameters
        {
            Properties properties = System.getProperties();
            PropertiesUtils.properties2Object(properties, fastConfig, CONFIG_JVM_PREFIX);
        }
        // 4. load runtime parameters
        {
            //--env=...
            if (args != null && args.length > 0) {
                Properties properties = new Properties();
                for (String arg : args) {
                    if (arg.startsWith("--") && arg.contains("=")) {
                        properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                    }
                }
                PropertiesUtils.properties2Object(properties, fastConfig);
            }
        }
        return fastConfig;
    }
}
