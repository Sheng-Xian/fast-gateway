package com.fast.gateway.core.netty.processor;

import com.fast.gateway.core.context.HttpRequestWrapper;

/**
 * @author sheng
 * @create 2023-06-20 17:29
 */
public interface NettyProcessor {
    public void process(HttpRequestWrapper httpRequestWrapper);
    public void start();
    public void shutdown();
}
