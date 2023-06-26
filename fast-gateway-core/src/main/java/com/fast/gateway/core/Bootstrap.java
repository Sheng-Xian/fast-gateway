package com.fast.gateway.core;

/**
 * @author sheng
 * @create 2023-06-18 21:47
 */
public class Bootstrap {
    public static void main(String[] args) {
        // 1. load gateway config
        FastConfig fastConfig = FastConfigLoader.getInstance().load(args);
        // 2. init filters
        // 3. init services registry management center
        // 4. start container
        FastContainer fastContainer = new FastContainer(fastConfig);
        fastContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                fastContainer.shutdown();
            }
        }));
    }
}
