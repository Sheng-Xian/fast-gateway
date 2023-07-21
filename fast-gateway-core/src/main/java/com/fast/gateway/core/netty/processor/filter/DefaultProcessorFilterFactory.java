package com.fast.gateway.core.netty.processor.filter;

import com.fast.gateway.common.util.ServiceLoader;
import com.fast.gateway.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author sheng
 * @create 2023-07-20 15:34
 */
@Slf4j
public class DefaultProcessorFilterFactory extends AbstractProcessorFilterFactory {

    private static class SingletonHolder {
        private static final DefaultProcessorFilterFactory INSTANCE = new DefaultProcessorFilterFactory();
    }
    public static DefaultProcessorFilterFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private DefaultProcessorFilterFactory() {

        // Load filter collection by SPI
        Map<String, List<ProcessorFilter<Context>>> filterMap = new LinkedHashMap<>();

        @SuppressWarnings("rawtypes")
        ServiceLoader<ProcessorFilter> serviceLoader = ServiceLoader.load(ProcessorFilter.class);

        for (ProcessorFilter<Context> filter : serviceLoader) {
            Filter annotation = filter.getClass().getAnnotation(Filter.class);
            if (annotation != null) {
                String filterType = annotation.value().getCode();
                List<ProcessorFilter<Context>> filterList = filterMap.get(filterType);
                if (filterList == null) {
                    filterList = new ArrayList<>();
                }
                filterList.add(filter);
                filterMap.put(filterType, filterList);
            }

            for (ProcessorFilterType filterType : ProcessorFilterType.values()) {
                List<ProcessorFilter<Context>> filterList = filterMap.get(filterType.getCode());
                if (filterList == null || filterList.isEmpty()) {
                    continue;
                }
                filterList.sort(new Comparator<ProcessorFilter<Context>>() {
                    @Override
                    public int compare(ProcessorFilter<Context> o1, ProcessorFilter<Context> o2) {
                        return o1.getClass().getAnnotation(Filter.class).order() -
                                o2.getClass().getAnnotation(Filter.class).order();
                    }
                });
                try {
                    super.buildFilterChain(filterType, filterList);
                } catch (Exception e) {
                    log.error("#DefaultProcessorFilterFactory.buildFilterChain# exception! Gateway filters loading has" +
                            "exception, error message is: {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void doFilterChain(Context context) throws Exception {
        try {
            defaultProcessorFilterChain.entry(context);
        } catch (Throwable e) {
            log.error("DefaultProcessorFilterFactory.doFilterChain# Error message {}", e.getMessage(), e);
            context.setThrowable(e);
            doErrorFilterChain(context);
        }
    }

    @Override
    public void doErrorFilterChain(Context context) throws Exception {
        try {
            errorProcessorFilterChain.entry(context);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doErrorFilterChain# Error message {}", e.getMessage(), e);
        }
    }

}
