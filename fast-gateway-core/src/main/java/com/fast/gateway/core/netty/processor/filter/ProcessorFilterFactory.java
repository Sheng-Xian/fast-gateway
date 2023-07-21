package com.fast.gateway.core.netty.processor.filter;

import com.fast.gateway.core.context.Context;

import java.util.List;

/**
 * ProcessorFilterFactory
 * @author sheng
 * @create 2023-07-18 14:03
 */
public interface ProcessorFilterFactory {
    // According to filter type, add a list of filters to build filterChain
    void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception;

    // Execute filter chains in normal cases
    void doFilterChain(Context context) throws Exception;

    // Execute this filter chain when hit error or exception
    void doErrorFilterChain(Context context) throws Exception;

    // Get specified class type filter
    <T> T getFilter(Class<T> t) throws Exception;

    // Get specified filter by ID
    <T> T getFilter(String filterId) throws Exception;
}
