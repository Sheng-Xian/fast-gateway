package com.fast.gateway.core.netty.processor;

import com.fast.gateway.common.concurrent.queue.flusher.ParallelFlusher;
import com.fast.gateway.common.enums.ResponseCode;
import com.fast.gateway.core.FastConfig;
import com.fast.gateway.core.context.HttpRequestWrapper;
import com.fast.gateway.core.helper.ResponseHelper;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sheng
 * @create 2023-06-21 16:01
 */
@Slf4j
public class NettyBatchEventProcess implements NettyProcessor{
    private FastConfig fastConfig;
    private NettyCoreProcessor nettyCoreProcessor;
    private ParallelFlusher<HttpRequestWrapper> parallelFlusher;
    private static final String THREAD_NAME_PREFIX = "fast-gateway-flusher-";

    public NettyBatchEventProcess(FastConfig fastConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.fastConfig = fastConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
                .setBufferSize(fastConfig.getBufferSize())
                .setThreads(fastConfig.getProcessThread())
                .setProducerType(ProducerType.MULTI)
                .setNamePrefix(THREAD_NAME_PREFIX)
                .setWaitStrategy(fastConfig.getATrueWaitStrategy());

        BatchEventProcessorListener batchEventProcessorListener = new BatchEventProcessorListener();
        builder.setEventListener(batchEventProcessorListener);
        this.parallelFlusher = builder.build();
    }
    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        System.err.println("NettyBatchEventProcessor is added.");
        this.parallelFlusher.add(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.nettyCoreProcessor.start();
        this.parallelFlusher.start();
    }

    @Override
    public void shutdown() {
        this.nettyCoreProcessor.shutdown();
        this.parallelFlusher.shutdown();
    }

    public class BatchEventProcessorListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {

        @Override
        public void onEvent(HttpRequestWrapper event) throws Exception {
            nettyCoreProcessor.process(event);
        }

        @Override
        public void onException(Throwable t, long sequence, HttpRequestWrapper event) {
            FullHttpRequest request = event.getFullHttpRequest();
            ChannelHandlerContext ctx = event.getCtx();
            try {
                log.error("#BatchEventProcessorListener# onException Request Handle Failing，request: {}. errorMessage: {}",
                        request, t.getMessage(), t);
                FullHttpResponse fullHttpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                if (!HttpUtil.isKeepAlive(request)) {
                    ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(fullHttpResponse);
                }
            } catch (Exception e) {
                log.error("#BatchEventProcessorListener# onException Request rewrite failed, request: {}. errorMessage: {},",
                        request, e.getMessage(), e);
            }
        }
    }
}
