package com.fast.gateway.core.netty;

import com.fast.gateway.common.util.RemotingHelper;
import com.fast.gateway.common.util.RemotingUtil;
import com.fast.gateway.core.FastConfig;
import com.fast.gateway.core.LifeCycle;
import com.fast.gateway.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Handle all network requests
 * @author sheng
 * @create 2023-06-20 16:26
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {
    private final FastConfig fastConfig;
    private int port = 8888;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventLoopGroupBoss;
    private EventLoopGroup eventLoopGroupWork;
    private NettyProcessor nettyProcessor;
    public NettyHttpServer(FastConfig fastConfig, NettyProcessor nettyProcessor) {
        this.fastConfig = fastConfig;
        this.nettyProcessor = nettyProcessor;
        if (fastConfig.getPort() > 0 && fastConfig.getPort() < 65535) {
            port = fastConfig.getPort();
        }
        init();
    }

    public EventLoopGroup getEventLoopGroupWork() {
        return eventLoopGroupWork;
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEPoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(fastConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("Netty Boss EPoll"));
            this.eventLoopGroupWork = new EpollEventLoopGroup(fastConfig.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("Netty Work Epoll"));
        } else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(fastConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("Netty Boss Nio"));
            this.eventLoopGroupWork = new NioEventLoopGroup(fastConfig.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("Netty Work Nio"));
        }
    }

    private boolean useEPoll() {
        return fastConfig.isUseEPoll() && RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        ServerBootstrap handler = this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWork)
                .channel(useEPoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 65535) //set size of sending data buffer area
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                .localAddress(new InetSocketAddress(this.port))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(fastConfig.getMaxContentLength()),
                                new HttpServerExpectContinueHandler(),
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler(nettyProcessor)
                                );
                    }
                });
        if (fastConfig.isNettyAllocator()) {
            handler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        try {
            serverBootstrap.bind().sync();
            log.info("-----Fast Gateway Start Up On Port " + this.port + "-----");
        } catch (Exception e) {
            throw new RuntimeException("serverBootstrap.bind().sync() failed" + e);
        }
    }

    @Override
    public void shutdown() {
        if (eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if (eventLoopGroupWork != null) {
            eventLoopGroupWork.shutdownGracefully();
        }
    }

    static class NettyServerConnectManagerHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty SERVER Pipeline: channel registered {}", remoteAddr);
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty SERVER Pipeline: channel unregistered {}", remoteAddr);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty SERVER Pipeline: Channel active {}", remoteAddr);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty SERVER Pipeline: Channel inactive {}", remoteAddr);
            ctx.fireChannelInactive();
        }
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.warn("Netty SERVER Pipeline: userEventTriggered: IDLE {}", remoteAddr);
                    ctx.channel().close();
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.warn("Netty Server Pipeline: remoteAddr: {}, exception caught {}", remoteAddr, cause);
            ctx.channel().close();
        }
    }
}
