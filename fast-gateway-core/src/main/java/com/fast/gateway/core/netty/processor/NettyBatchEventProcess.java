package com.fast.gateway.core.netty.processor;

import com.fast.gateway.core.FastConfig;
import com.fast.gateway.core.context.HttpRequestWrapper;

/**
 * @author sheng
 * @create 2023-06-21 16:01
 */
public class NettyBatchEventProcess implements NettyProcessor{
    private FastConfig fastConfig;
    private NettyCoreProcessor nettyCoreProcessor;
    public NettyBatchEventProcess(FastConfig fastConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.fastConfig = fastConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
    }
    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
