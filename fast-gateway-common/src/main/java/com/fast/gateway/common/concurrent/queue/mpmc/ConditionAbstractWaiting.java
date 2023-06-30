package com.fast.gateway.common.concurrent.queue.mpmc;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

/**
 * @author sheng
 * @create 2023-06-30 17:35
 */
public abstract class ConditionAbstractWaiting implements Condition{
    private static final int CACHE_LINE_REFS = Contended.CACHE_LINE / Long.BYTES;
    private static final int MAX_WAITERS = 8;
    private static final long WAITER_MASK = MAX_WAITERS - 1L;
    private static final long WAIT_TIME = PARK_TIMEOUT;
    private final LongAdder waitCount = new LongAdder();
    private final AtomicReferenceArray<Thread> waiter = new AtomicReferenceArray<>(MAX_WAITERS + 2 * CACHE_LINE_REFS);
    private long waitCache = 0L;

    @Override
    public abstract boolean test();

    @Override
    public void awaitNanos(long timeout) throws InterruptedException {
        for (;;) {
            try {
                final long waitCount = this.waitCount.sum();
                long waitSequence = waitCount;
                this.waitCount.increment();
                waitCache = waitCount + 1;

                long timeNow = System.nanoTime();
                final long expires = timeNow + timeout;

                final Thread t = Thread.currentThread();

                if (waitCount == 0) {
                    int spin = 0;
                    while (test() && expires > timeNow && !t.isInterrupted()) {
                        spin = Condition.progressiveYield(spin);
                        timeNow = System.nanoTime();
                    }

                    if (t.isInterrupted()) {
                        throw new InterruptedException();
                    }
                    return;
                } else {
                    int spin = 0;
                    while (test() && !waiter.compareAndSet((int)(waitSequence++ & WAITER_MASK) + CACHE_LINE_REFS,
                            null, t) && expires > timeNow) {
                        if (spin < Condition.MAX_PROG_YIELD) {
                            spin = Condition.progressiveYield(spin);
                        } else {
                            LockSupport.parkNanos(MAX_WAITERS * Condition.PARK_TIMEOUT);
                        }
                        timeNow = System.nanoTime();
                    }
                    while (test() && (waiter.get((int)((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS) == t) &&
                            expires > timeNow && !t.isInterrupted()) {
                        LockSupport.parkNanos((expires - timeNow) >> 2);
                        timeNow = System.nanoTime();
                    }
                    if (t.isInterrupted()) {
                        while (!waiter.compareAndSet((int)((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS, t,
                                null) && waiter.get(CACHE_LINE_REFS) == t) {
                            LockSupport.parkNanos(PARK_TIMEOUT);
                        }
                        throw new InterruptedException();
                    }
                    return;
                }
            } finally {
                waitCount.decrement();
                waitCache = waitCount.sum();
            }
        }
    }

    @Override
    public void await() throws InterruptedException {
        for (;;) {
            try {
                final long waitCount = this.waitCount.sum();
                long waitSequence = waitCount;
                this.waitCount.increment();
                waitCache = waitCount + 1;

                final Thread t = Thread.currentThread();

                if (waitCount == 0) {
                    int spin = 0;
                    while (test() && !t.isInterrupted()) {
                        spin = Condition.progressiveYield(spin);
                    }
                    if (t.isInterrupted()) {
                        throw new InterruptedException();
                    }
                    return;
                } else {
                    int spin = 0;
                    while (test() && !waiter.compareAndSet((int)(waitSequence++ & WAITER_MASK) + CACHE_LINE_REFS, null,
                            t) && !t.isInterrupted()) {
                        if (spin < Condition.MAX_PROG_YIELD) {
                            spin = Condition.progressiveYield(spin);
                        } else {
                            LockSupport.parkNanos(MAX_WAITERS * Condition.PARK_TIMEOUT);
                        }
                    }
                    while (test() && (waiter.get((int)((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS) == t) &&
                    !t.isInterrupted()) {
                        LockSupport.parkNanos(1_000_000L);
                    }
                    if (t.isInterrupted()) {
                        while (!waiter.compareAndSet((int)((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS, t,
                                null) && waiter.get(CACHE_LINE_REFS) == t) {
                            LockSupport.parkNanos(WAIT_TIME);
                        }
                        throw new InterruptedException();
                    }
                    return;
                }
            } finally {
                waitCount.decrement();
                waitCache = waitCount.sum();
            }
        }
    }

    @Override
    public void signal() {
        if (waitCache > 0 || (waitCache = waitCount.sum()) > 0) {
            long waitSequence = 0L;
            for (;;) {
                Thread t;
                while ((t = waiter.get((int)(waitSequence++ & WAITER_MASK) + CACHE_LINE_REFS)) != null) {
                    if (waiter.compareAndSet((int)((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS, t, null)) {
                        LockSupport.unpark(t);
                    } else {
                        LockSupport.parkNanos((WAIT_TIME));
                    }
                    if (((waitSequence & WAITER_MASK) == WAITER_MASK) || (waitCache = waitCount.sum()) == 0) {
                        return;
                    }
                }
            }
        }
    }
}
