package com.fast.gateway.core.netty.processor;

import com.fast.gateway.core.context.HttpRequestWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author sheng
 * @create 2023-06-21 15:10
 */
public class NettyCoreProcessor implements NettyProcessor{
    @Override
    public void process(HttpRequestWrapper event) {
        FullHttpRequest request = event.getFullHttpRequest();
        ChannelHandlerContext ctx = event.getCtx();
        try {
            // 1. parse FullHttpRequest
            // 2. execute FilterChain
            System.out.println("Received HTTP request");
        } catch (Throwable t) {

        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
