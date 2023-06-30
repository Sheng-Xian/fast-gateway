package com.fast.gateway.common.concurrent.queue.mpmc;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author sheng
 * @create 2023-06-28 18:35
 */
public class ContendedAtomicLong extends Contended{
    private static final int CACHE_LINE_LONGS = CACHE_LINE / Long.BYTES;

    private final AtomicLongArray contendedArray;

    ContendedAtomicLong(final long init) {
        contendedArray = new AtomicLongArray(2 * CACHE_LINE_LONGS);
        set(init);
    }

    void set(final long l) {
        contendedArray.set(CACHE_LINE_LONGS, l);
    }

    long get() {
        return contendedArray.get(CACHE_LINE_LONGS);
    }

    public String toString() {
        return Long.toString(get());
    }

    public boolean compareAndSet(final long expect, final long l) {
        return contendedArray.compareAndSet(CACHE_LINE_LONGS, expect, l);
    }
}
