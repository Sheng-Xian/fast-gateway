package com.fast.gateway.core.netty.processor.filter;

import com.fast.gateway.common.config.Rule;
import com.fast.gateway.common.constants.BasicConst;
import com.fast.gateway.common.util.JSONUtil;
import com.fast.gateway.core.context.Context;
import com.fast.gateway.core.netty.processor.cache.DefaultCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sheng
 * @create 2023-07-20 23:29
 */
@Slf4j
public abstract class AbstractEntryProcessorFilter<FilterConfigClass> extends AbstractLinkedProcessorFilter<Context> {

    protected Filter filterAnnotation;

    // Each JSON map a entity class, call it FilterConfigClass
    protected Cache<String, FilterConfigClass> cache;

    protected final Class<FilterConfigClass> filterConfigClass;

    protected AbstractEntryProcessorFilter(Class<FilterConfigClass> filterConfigClass) {
        this.filterAnnotation = this.getClass().getAnnotation(Filter.class);
        this.filterConfigClass = filterConfigClass;
        this.cache = DefaultCacheManager.getInstance().create(DefaultCacheManager.FILTER_CONFIG_CACHE_ID);
    }

    @Override
    public boolean check(Context context) throws Throwable {
        return context.getRule().hasFilterId(filterAnnotation.id());
    }

    @Override
    public void transformEntry(Context context, Object... args) throws Throwable {
        FilterConfigClass filterConfigClass = dynamicLoadFilterConfigCache(context, args);
        super.transformEntry(context, filterConfigClass);
    }

    /**
     * Dynamic loading cache: each filter specific config rule
     * @param context - context
     * @param args - other parameters may be used
     */
    private FilterConfigClass dynamicLoadFilterConfigCache(Context context, Object[] args) {
        Rule.FilterConfig filterConfig = context.getRule().getFilterConfig(filterAnnotation.id());
        String ruleId = context.getRule().getId();
        String cacheKey = ruleId + BasicConst.DOLLAR_SEPARATOR + filterAnnotation.id();
        FilterConfigClass tempFilterConfigClass = cache.getIfPresent(cacheKey);
        if (tempFilterConfigClass == null) {
            if (filterConfig != null && StringUtils.isNotEmpty(filterConfig.getConfig())) {
                String configStr = filterConfig.getConfig();
                try {
                    tempFilterConfigClass = JSONUtil.parse(configStr, filterConfigClass);
                    cache.put(cacheKey, tempFilterConfigClass);
                } catch (Exception e) {
                    log.error("#AbstractEntryProcessorFilter# dynamicLoadCache filterId; {}, config parse error {}",
                            filterAnnotation.id(), configStr, e);
                }
            }
        }
        return tempFilterConfigClass;
    }

}
