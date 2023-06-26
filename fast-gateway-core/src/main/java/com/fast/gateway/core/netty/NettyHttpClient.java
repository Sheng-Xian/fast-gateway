package com.fast.gateway.core.netty;

import com.fast.gateway.core.FastConfig;
import com.fast.gateway.core.LifeCycle;
import com.fast.gateway.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

/**
 * @author sheng
 * @create 2023-06-20 16:27
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {
    private FastConfig fastConfig;
    private AsyncHttpClient asyncHttpClient;
    private DefaultAsyncHttpClientConfig.Builder clientBuilder;
    private EventLoopGroup eventLoopGroupWork;
    public NettyHttpClient(FastConfig fastConfig, EventLoopGroup eventLoopGroupWork) {
        this.fastConfig = fastConfig;
        this.eventLoopGroupWork = eventLoopGroupWork;
        init();
    }
    @Override
    public void init() {
        this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(eventLoopGroupWork)
                .setConnectTimeout(fastConfig.getHttpConnectTimeout())
                .setRequestTimeout(fastConfig.getHttpRequestTimeout())
                .setMaxRequestRetry(fastConfig.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(fastConfig.getHttpMaxConnections())
                .setMaxConnectionsPerHost(fastConfig.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(fastConfig.getHttpPooledConnectionIdleTimeout());
    }

    @Override
    public void start() {
        this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                log.error("#NettyHttpClient.shutdown# shutdown error", e);
            }
        }
    }
}
