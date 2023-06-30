package com.fast.gateway.common.concurrent.queue.mpmc;

/**
 * @author sheng
 * @create 2023-06-28 18:36
 */
public class Contended {
    public static final int CACHE_LINE = Integer.getInteger("Intel.CacheLineSize", 64); // bytes
}
