package com.fast.gateway.core.netty.processor.filter;

/**
 * @author sheng
 * @create 2023-07-18 13:24
 */
public abstract class ProcessorFilterChain<T> extends AbstractLinkedProcessorFilter<T> {
    // Add element at the head of chain
    public abstract void addFirst(AbstractLinkedProcessorFilter<T> filter);
    // Add element at the end of chain
    public abstract void addLast(AbstractLinkedProcessorFilter<T> filter);
}
