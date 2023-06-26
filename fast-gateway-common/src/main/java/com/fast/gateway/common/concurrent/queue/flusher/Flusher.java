package com.fast.gateway.common.concurrent.queue.flusher;

/**
 * @author sheng
 * @create 2023-06-26 21:12
 */
public interface Flusher<E> {
    void add(E event);
    void add(E... event);
    boolean tryAdd(E event);
    boolean tryAdd(E... event);
    boolean isShutdown();
    void start();
    void shutdown();
}
