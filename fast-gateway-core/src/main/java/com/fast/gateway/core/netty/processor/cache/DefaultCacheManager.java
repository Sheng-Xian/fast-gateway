package com.fast.gateway.core.netty.processor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sheng
 * @create 2023-07-20 23:38
 */
public class DefaultCacheManager {

    private DefaultCacheManager() {
    }

    public static final String FILTER_CONFIG_CACHE_ID = "filterConfigCache";

    // Double layers cache
    private final ConcurrentHashMap<String, Cache<String, ?>> cacheMap = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        private static final DefaultCacheManager INSTANCE = new DefaultCacheManager();
    }

    public static DefaultCacheManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * According to cache id to create a cache object
     * @param cacheID - cache id
     * @return cache object
     * @param <V> - V object which cache is created for
     */
    @SuppressWarnings("unchecked")
    public <V> Cache<String, V> create(String cacheID) {
        Cache<String, V> cache = Caffeine.newBuilder().build();
        cacheMap.put(cacheID, cache);
        return (Cache<String, V>) cacheMap.get(cacheID);
    }

    /**
     * According to cache id and cache key to delete a cache object
     * @param cacheId - cache id
     * @param key - cache key
     * @param <V> - V object which cache is created for
     */
    public <V> void remove(String cacheId, String key) {
        @SuppressWarnings("unchecked")
        Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * According to a global cache id, delete the cache object
     * @param cacheId - cache id
     * @param <V> - V object which cache is created for
     */
    public <V> void remove(String cacheId) {
        @SuppressWarnings("unchecked")
        Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * remove all cache objects in cashMap
     */
    public void cleanAll() {
        cacheMap.values().forEach(Cache::invalidateAll);
    }
}
