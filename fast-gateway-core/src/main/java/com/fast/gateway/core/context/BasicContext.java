package com.fast.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author sheng
 * @create 2023-07-02 11:51
 */
public abstract class BasicContext implements Context{

    protected final String protocol;

    protected final ChannelHandlerContext nettyContext;

    protected final boolean keepAlive;

    protected volatile int status = Context.RUNNING;

    protected Throwable throwable;

    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    protected final Map<AttributeKey<?>, Object> attributes = new HashMap<AttributeKey<?>, Object>();

    protected List<Consumer<Context>> completedCallbacks;

    public BasicContext(String protocol, ChannelHandlerContext nettyContext, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyContext = nettyContext;
        this.keepAlive = keepAlive;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public ChannelHandlerContext getNettyContext() {
        return this.nettyContext;
    }

    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override
    public void run() {
        this.status = Context.RUNNING;
    }

    @Override
    public void written() {
        this.status = Context.WRITTEN;
    }

    @Override
    public void completed() {
        this.status = Context.COMPLETED;
    }

    @Override
    public void terminated() {
        this.status = Context.TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return this.status == Context.RUNNING;
    }

    @Override
    public boolean isWritten() {
        return this.status == Context.WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return this.status == Context.COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return this.status == Context.TERMINATED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(AttributeKey<T> key) {
        return (T) attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T putAttribute(AttributeKey<T> key, T value) {
        return (T) attributes.put(key, value);
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    // Identify if resource is release, label a threshold
    @Override
    public void releaseRequest() {
        this.requestReleased.compareAndSet(false, true);
    }

    @Override
    public void completedCallback(Consumer<Context> consumer) {
        if (completedCallbacks == null) {
            completedCallbacks = new ArrayList<>();
        }
        completedCallbacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallback() {
        if (completedCallbacks != null) {
            completedCallbacks.forEach(call -> call.accept(this));
        }
    }
}