package com.fast.gateway.core.netty.processor;

import com.fast.gateway.common.concurrent.queue.mpmc.MpmcBlockingQueue;
import com.fast.gateway.common.enums.ResponseCode;
import com.fast.gateway.core.FastConfig;
import com.fast.gateway.core.context.HttpRequestWrapper;
import com.fast.gateway.core.helper.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sheng
 * @create 2023-06-21 16:01
 */
@Slf4j
public class NettyMpmcProcessor implements NettyProcessor{
    private FastConfig fastConfig;
    private NettyCoreProcessor nettyCoreProcessor;
    private MpmcBlockingQueue<HttpRequestWrapper> mpmcBlockingQueue;
    private boolean usedExecutorPool;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    private Thread consumerProcessorThread;

    public NettyMpmcProcessor(FastConfig fastConfig, NettyCoreProcessor nettyCoreProcessor, boolean usedExecutorPool) {
        this.fastConfig = fastConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        this.mpmcBlockingQueue = new MpmcBlockingQueue<>(fastConfig.getBufferSize());
        this.usedExecutorPool = usedExecutorPool;
    }
    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) throws Exception {
        System.err.println("NettyMpmcProcessor put!");
        this.mpmcBlockingQueue.put(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.isRunning = true;
        this.nettyCoreProcessor.start();
        if (usedExecutorPool) {
            this.executorService = Executors.newFixedThreadPool(this.fastConfig.getProcessThread());
            for (int i = 0; i < fastConfig.getProcessThread(); i++) {
                this.executorService.submit(new ConsumerProcessor());
            }
        } else {
            this.consumerProcessorThread = new Thread(new ConsumerProcessor());
            this.consumerProcessorThread.start();
        }
    }

    @Override
    public void shutdown() {
        this.isRunning = false;
        this.nettyCoreProcessor.shutdown();
        if (usedExecutorPool) {
            this.executorService.shutdown();
        }
    }

    public class ConsumerProcessor implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                HttpRequestWrapper event = null;
                try {
                    event = mpmcBlockingQueue.take();
                    nettyCoreProcessor.process(event);
                } catch (Throwable t) {
                    if (event != null) {
                        HttpRequest request = event.getFullHttpRequest();
                        ChannelHandlerContext ctx = event.getCtx();
                        try {
                            log.error("#ConsumerProcessor# onException request handling failed, request: {}. errorMessage: {}",
                                    request, t.getMessage(), t);
                            FullHttpResponse fullHttpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                            if (!HttpUtil.isKeepAlive(request)) {
                                ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                            } else {
                                fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                ctx.writeAndFlush(fullHttpResponse);
                            }
                        } catch (Exception e) {
                            log.error("#ConsumerProcessor# on Exception request rewrite and flush failed, request: {}. errorMessage: {}",
                                    request, e.getMessage(), e);
                        }
                    } else {
                        log.error("#ConsumerProcessor# onException event is empty. errorMessage {}", t.getMessage(), t);
                    }
                }
            }
        }
    }
}
