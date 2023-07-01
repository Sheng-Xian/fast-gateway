package com.fast.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * @author sheng
 * @create 2023-07-01 9:38
 */
public interface Context {
    // a request is executing
    int RUNNING = -1;
    // filter chain is completed, going to write and flush context
    int WRITTEN = 0;
    // written successfully, context.writeAndFlush(response), avoid write multiple times;
    int COMPLETED = 1;
    // after post filters completed
    int TERMINATED = 2;

    // set status
    void run();

    void written();

    void completed();

    void terminated();

    boolean isRunning();

    boolean isWritten();

    boolean isCompleted();

    boolean isTerminated();

    String getProtocal();

    Object getRule();

    Object getRequest();

    Object getResponse();

    //Builder to build request, through Context to set Response
    void setResponse(Object Response);

    void setThrowable(Throwable throwable);

    Throwable getThrowable();

    <T> T getAttribute(AttributeKey<T> key);

    <T> T putAttribute(AttributeKey<T> key, T value);

    // need to use netty context to write and flush
    ChannelHandlerContext getNettyContext();

    boolean isKeepAlive();

    void releaseRequest(); // The NettyHttpServerHandler doesn't release automatically.

    // After written ended, set callback function
    void completedCallback(Consumer<Context> consumer);

    //回调函数设置
    // setup callback function
    void invokeCompletedCallback();
}
