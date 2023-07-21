package com.fast.gateway.core.netty.processor.filter;

import com.fast.gateway.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.fast.gateway.core.netty.processor.filter.ProcessorFilterType.*;

/**
 * @author sheng
 * @create 2023-07-18 16:06
 */
@Slf4j
public abstract class AbstractProcessorFilterFactory implements ProcessorFilterFactory {

    /*
     * pre + route + post
     */
    public DefaultProcessorFilterChain defaultProcessorFilterChain =
            new DefaultProcessorFilterChain("defaultProcessorFilterChain");

    /*
     * Errors happened in post would be ignored. Errors happened in pre or route would be handled.
     * error + post
     */
    public DefaultProcessorFilterChain errorProcessorFilterChain =
            new DefaultProcessorFilterChain("errorProcessorFilterChain");

    /*
     * According to filters' type to get filter collection
     */
    public Map<String /* processorFilterType*/, Map<String /* filterId */, ProcessorFilter<Context>>>
            processorFilterTypeMap = new LinkedHashMap<>();

    /*
     * According to filters' id to get filter
     */
    public Map<String /* filterId */, ProcessorFilter<Context>> processorFilterIdMap = new LinkedHashMap<>();

    @Override
    public void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception {
        switch (filterType) {
            case PRE:
            case ROUTE:
                addFilterForChain(defaultProcessorFilterChain, filters);
                break;
            case ERROR:
                addFilterForChain(errorProcessorFilterChain, filters);
                break;
            case POST:
                addFilterForChain(defaultProcessorFilterChain, filters);
                addFilterForChain(errorProcessorFilterChain, filters);
            default:
                throw new RuntimeException("ProcessorFilterType is not supported! ");
        }
    }

    private void addFilterForChain(DefaultProcessorFilterChain processorFilterChain,
                                   List<ProcessorFilter<Context>> filters) throws Exception{
        for(ProcessorFilter<Context> processorFilter : filters) {
            processorFilter.init();
            doBuilder(processorFilterChain, processorFilter);
        }
    }

    /**
     * Add filter into specific filterChain
     * @param processorFilterChain - Specific Processor FilterChain
     * @param processorFilter - Individual Processor Filter
     */
    private void doBuilder(DefaultProcessorFilterChain processorFilterChain,
                           ProcessorFilter<Context> processorFilter) {
        log.info("filterChain: {}, the scanner filter is {}",
                processorFilterChain.getId(), processorFilter.getClass().getName());
        Filter annotation = processorFilter.getClass().getAnnotation(Filter.class);
        if (annotation != null) {
            processorFilterChain.addLast((AbstractLinkedProcessorFilter<Context>) processorFilter);
            // map to filters' collection
            String filterId = annotation.id();
            if (filterId == null || filterId.length() < 1) {
                filterId = processorFilter.getClass().getName();
            }
            String code = annotation.value().getCode();
            Map<String, ProcessorFilter<Context>> filterMap = processorFilterTypeMap.get(code);
            if (filterMap == null) {
                filterMap = new LinkedHashMap<String, ProcessorFilter<Context>>();
            }
            filterMap.put(filterId, processorFilter);
            processorFilterTypeMap.put(code, filterMap);
            processorFilterIdMap.put(filterId, processorFilter);
         }
    }

    @Override
    public <T> T getFilter(Class<T> t) throws Exception {
        Filter annotation = t.getAnnotation(Filter.class);
        if (annotation != null) {
            String filterId = annotation.id();
            if (filterId == null || filterId.length() < 1) {
                filterId = t.getName();
            }
            return this.getFilter(filterId);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFilter(String filterId) throws Exception {
        ProcessorFilter<Context> filter = null;
        if (!processorFilterIdMap.isEmpty()) {
            filter = processorFilterIdMap.get(filterId);
        }
        return (T) filter;
    }
}
