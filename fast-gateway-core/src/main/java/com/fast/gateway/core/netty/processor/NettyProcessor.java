package com.fast.gateway.core.netty.processor;

import com.fast.gateway.core.context.HttpRequestWrapper;

/**
 * @author sheng
 * @create 2023-06-20 17:29
 */
public interface NettyProcessor {
    void process(HttpRequestWrapper httpRequestWrapper) throws Exception;
    void start();
    void shutdown();
}
