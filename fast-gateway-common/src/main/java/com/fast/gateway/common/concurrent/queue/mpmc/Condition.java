package com.fast.gateway.common.concurrent.queue.mpmc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author sheng
 * @create 2023-06-28 19:18
 */
public interface Condition {
    long PARK_TIMEOUT = 50L;
    int MAX_PROG_YIELD = 2000;
    boolean test ();

    // wake me when the condition is satisfied, or timeout
    void awaitNanos (final long timeout) throws InterruptedException;

    // wake if signal is called, or wait indefinitely
    void await () throws InterruptedException;

    // tell threads waiting on condition to wake up
    void signal ();

    // progressively transition from spin to yield over time
    static int progressiveYield (final int n) {
        if (n > 500) {
            if (n < 1000) {
                if ((n & 0x7) == 0) {
                    LockSupport.parkNanos(PARK_TIMEOUT);
                } else {
                    onSpinWait();
                }
            } else if (n < MAX_PROG_YIELD) {
                if ((n & 0x3) == 0) {
                    Thread.yield();
                } else {
                    onSpinWait();
                }
            } else {
                Thread.yield();
                return n;
            }
        } else {
            onSpinWait();
        }
        return n + 1;
    }

    static void onSpinWait() {

    }

    static boolean waitStatus (final long timeout, final TimeUnit unit, final Condition condition) throws InterruptedException {
        final long timeoutNanos = TimeUnit.NANOSECONDS.convert(timeout, unit);
        final long expireTime = System.nanoTime() + timeoutNanos;
        while (condition.test()) {
            final long now = System.nanoTime();
            if (now > expireTime) {
                return false;
            }
            condition.awaitNanos(expireTime - now - PARK_TIMEOUT);
        }
        return true;
    }
}
