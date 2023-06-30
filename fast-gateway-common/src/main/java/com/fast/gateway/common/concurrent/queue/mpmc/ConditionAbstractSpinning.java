package com.fast.gateway.common.concurrent.queue.mpmc;

/**
 * @author sheng
 * @create 2023-06-30 16:52
 */
public abstract class ConditionAbstractSpinning implements Condition{

    @Override
    public void awaitNanos(long timeout) throws InterruptedException {
        long timeNow = System.nanoTime();
        final long expires = timeNow + timeout;

        final Thread t = Thread.currentThread();

        while (test() && expires > timeNow && !t.isInterrupted()) {
            timeNow = System.nanoTime();
            Condition.onSpinWait();
        }

        if (t.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void await() throws InterruptedException {
        final Thread t = Thread.currentThread();

        while (test() && !t.isInterrupted()) {
            Condition.onSpinWait();
        }

        if (t.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void signal() {

    }
}
