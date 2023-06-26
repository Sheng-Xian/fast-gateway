package com.fast.gateway.core.netty;

import com.fast.gateway.core.context.HttpRequestWrapper;
import com.fast.gateway.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sheng
 * @create 2023-06-21 0:44
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private NettyProcessor nettyProcessor;
    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
            httpRequestWrapper.setFullHttpRequest(request);
            httpRequestWrapper.setCtx(ctx);
            nettyProcessor.process(httpRequestWrapper);
        } else {
            log.error("#NettyHttpServerHandler.channelRead# message type is not httpRequest: {}", msg);
            boolean release = ReferenceCountUtil.release(msg);
            if (!release) {
                log.error("#NettyHttpServerHandler.channelRead# message release fail");
            }
        }
    }
}
