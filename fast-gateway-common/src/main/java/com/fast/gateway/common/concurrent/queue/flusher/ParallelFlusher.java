package com.fast.gateway.common.concurrent.queue.flusher;

import com.google.common.base.Preconditions;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.EventListener;

/**
 * @author sheng
 * @create 2023-06-26 23:07
 */
public class ParallelFlusher<E> implements Flusher<E> {
    private ParallelFlusher(Builder<E> builder) {

    }

    @Override
    public void add(Object event) {

    }

    @Override
    public void add(Object[] event) {

    }

    @Override
    public boolean tryAdd(Object event) {
        return false;
    }

    @Override
    public boolean tryAdd(Object[] event) {
        return false;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    public interface EventListener<E> {
        void onEvent(E event) throws Exception;

        void onException(Throwable ex, long sequence, E event);
    }

     // Builder pattern for creating ParallelFlusher object
     public static class Builder<E> {
        private ProducerType producerType = ProducerType.MULTI;
        private int bufferSize = 16 * 1024;
        private int threads = 1;
        private String namePrefix = "";
        private WaitStrategy waitStrategy = new BlockingWaitStrategy();
        private EventListener<E> listener;

        public Builder<E> setProducerType(ProducerType producerType) {
            Preconditions.checkNotNull(producerType);
            this.producerType = producerType;
            return this;
        }

        public Builder<E> setThreads(int threads) {
            Preconditions.checkArgument(threads > 0);
            this.threads = threads;
            return this;
        }

        public Builder<E> setBufferSize(int bufferSize) {
            Preconditions.checkArgument(Integer.bitCount(bufferSize) == 1);
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<E> setNamePrefix(String namePrefix) {
            Preconditions.checkNotNull(namePrefix);
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder<E> setWaitStrategy(WaitStrategy waitStrategy) {
            Preconditions.checkNotNull(waitStrategy);
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder<E> setEventListener(EventListener<E> listener) {
            Preconditions.checkNotNull(listener);
            this.listener = listener;
            return this;
        }

        public ParallelFlusher<E> build() {
            return new ParallelFlusher<>(this);
        }
     }
}
