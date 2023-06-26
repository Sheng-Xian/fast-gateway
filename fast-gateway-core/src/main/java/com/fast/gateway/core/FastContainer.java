package com.fast.gateway.core;

import com.fast.gateway.common.constants.FastBufferHelper;
import com.fast.gateway.core.netty.NettyHttpClient;
import com.fast.gateway.core.netty.NettyHttpServer;
import com.fast.gateway.core.netty.processor.NettyBatchEventProcess;
import com.fast.gateway.core.netty.processor.NettyCoreProcessor;
import com.fast.gateway.core.netty.processor.NettyMpmcProcessor;
import com.fast.gateway.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sheng
 * @create 2023-06-21 15:40
 */
@Slf4j
public class FastContainer {
    private final FastConfig fastConfig;
    private NettyProcessor nettyProcessor;
    private NettyHttpServer nettyHttpServer;
    private NettyHttpClient nettyHttpClient;

    public FastContainer(FastConfig fastConfig) {
        this.fastConfig = fastConfig;
        init();
    }

    public void init() {
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        String bufferType = fastConfig.getBufferType();
        if (FastBufferHelper.isFlusher(bufferType)) {
            nettyProcessor = new NettyBatchEventProcess(fastConfig, nettyCoreProcessor);
        } else if (FastBufferHelper.isMpmc(bufferType)) {
            this.nettyProcessor = new NettyMpmcProcessor(fastConfig, nettyCoreProcessor);
        } else {
            nettyProcessor = nettyCoreProcessor;
        }
        nettyHttpServer = new NettyHttpServer(fastConfig, nettyProcessor);
        nettyHttpClient = new NettyHttpClient(fastConfig, nettyHttpServer.getEventLoopGroupWork());
    }

    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("Fast Gateway started! ");
    }

    public void shutdown() {
        nettyProcessor.shutdown();
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}
