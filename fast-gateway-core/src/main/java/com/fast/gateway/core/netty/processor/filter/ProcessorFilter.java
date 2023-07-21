package com.fast.gateway.core.netty.processor.filter;

/**
 * A interface to execute filters, SPI most basic interface implementation
 * @author sheng
 * @create 2023-07-14 20:06
 */
public interface ProcessorFilter<T> {
    // To define if filter is executed.
    boolean check(T t) throws Throwable;

    // To execute filters
    void entry(T t, Object... args) throws Throwable;

    // Trigger next filters' execution
    void fireNext(T t, Object... args) throws Throwable;

    // Method to transfer object
    void transformEntry(T t, Object... args) throws Throwable;

    // Init filters if child class needs then override
    default void init() throws Exception {
    }

    // Destroy filters if child class needs then override
    default void destroy() throws Exception {
    }

    // Refresh filters if child class needs then override
    default void refresh() throws Exception {
    }
}
