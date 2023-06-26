package com.fast.gateway.core;

/**
 * @author sheng
 * @create 2023-06-20 16:24
 */
public interface LifeCycle {
    void init();
    void start();
    void shutdown();
}
