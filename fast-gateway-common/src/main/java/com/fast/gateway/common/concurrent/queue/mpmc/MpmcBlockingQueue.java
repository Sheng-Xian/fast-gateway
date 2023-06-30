package com.fast.gateway.common.concurrent.queue.mpmc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author sheng
 * @create 2023-06-28 19:11
 */
public final class MpmcBlockingQueue<E> extends MpmcConcurrentQueue<E> implements Serializable, Iterable<E>, Collection<E>,
        BlockingQueue<E>, Queue<E>, ConcurrentQueue<E> {

    private static final long serialVersionUID = -5010414655037152451L;
    private final Condition queueNotFullCondition;
    private final Condition queueNotEmptyCondition;

    public MpmcBlockingQueue(final int capacity) {
        this(capacity, SpinPolicy.WAITING);
    }

    public MpmcBlockingQueue(final int capacity, final SpinPolicy spinPolicy) {
        super(capacity);
        switch (spinPolicy) {
            case BLOCKING:
                queueNotFullCondition = new QueueNotFull();
                queueNotEmptyCondition = new QueueNotEmpty();
                break;
            case SPINNING:
                queueNotFullCondition = new SpinningQueueNotFull();
                queueNotEmptyCondition = new SpinningQueueNotEmpty();
                break;
            case WAITING:
            default:
                queueNotFullCondition = new WaitingQueueNotFull();
                queueNotEmptyCondition = new WaitingQueueNotEmpty();
        }
    }

    public MpmcBlockingQueue(final int capacity, Collection<? extends E> c) {
        this(capacity);
        for (final E e : c) {
            offer(e);
        }
    }

    @Override
    public final boolean offer(E e) {
        if (super.offer(e)) {
            queueNotEmptyCondition.signal();
            return true;
        } else {
            queueNotEmptyCondition.signal();
            return false;
        }
    }

    @Override
    public final E poll() {
        final E e = super.poll();
        queueNotFullCondition.signal();
        return  e;
    }

    @Override
    public int remove(final E[] e) {
        final int n = super.remove(e);
        queueNotFullCondition.signal();
        return n;
    }

    @Override
    public void put(E e) throws InterruptedException {
        while (offer(e) == false) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            queueNotFullCondition.await();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        for (;;) {
            if (offer(e)) {
                return true;
            } else {
                if (!Condition.waitStatus(timeout, unit, queueNotFullCondition)) return false;
            }
        }
    }

    @Override
    public E take() throws InterruptedException {
        for (;;) {
            E pollObj = poll();
            if (pollObj != null) {
                return pollObj;
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            queueNotEmptyCondition.await();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        for (;;) {
            E pollObj = poll();
            if (pollObj != null) {
                return pollObj;
            } else {
                if (!Condition.waitStatus(timeout, unit, queueNotEmptyCondition)) return null;
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        queueNotFullCondition.signal();
    }

    @Override
    public int remainingCapacity() {
        return size - size();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, size());
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (this == c) throw new IllegalArgumentException("Can not drain to self.");

        int nRead = 0;

        while (!isEmpty() && maxElements > 0) {
            final E e = poll();
            if (e != null) {
                c.add(e);
                nRead++;
            }
        }
        return nRead;
    }

    @Override
    public E remove() {
        return poll();
    }

    @Override
    public E element() {
        final E val = peek();
        if (val != null) return val;
        throw new NoSuchElementException("No element found.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray() {
        final E[] e = (E[]) new Object[size()];
        toArray(e);
        return e;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        remove((E[]) a);
        return a;
    }

    @Override
    public boolean add(E e) {
        if (offer(e)) return true;
        throw new IllegalStateException("queue is full");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (final Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (final E e : c) {
            if (!offer(e)) return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new RingItr();
    }

    private final class RingItr implements Iterator<E> {
        int dx = 0;
        E lastObj = null;

        private RingItr() {

        }

        @Override
        public boolean hasNext() {
            return dx < size();
        }

        @Override
        public E next() {
            final long pollPos = head.get();
            final int slot = (int) ((pollPos + dx++) & mask);
            lastObj = buffer[slot].entry;
            return lastObj;
        }

        @Override
        public void remove() {
            MpmcBlockingQueue.this.remove(lastObj);
        }
    }

    private boolean isFull() {
        return tail.get() - head.get() == size;
    }

    private final class QueueNotFull extends ConditionAbstract {

        @Override
        public boolean test() {
            return isFull();
        }
    }

    private final class QueueNotEmpty extends ConditionAbstract {

        @Override
        public boolean test() {
            return isEmpty();
        }
    }

    private final class WaitingQueueNotFull extends ConditionAbstractWaiting {

        @Override
        public boolean test() {
            return isFull();
        }
    }

    private final class WaitingQueueNotEmpty extends ConditionAbstractWaiting {

        @Override
        public boolean test() {
            return isEmpty();
        }
    }

    private final class SpinningQueueNotFull extends ConditionAbstractSpinning {

        @Override
        public boolean test() {
            return isFull();
        }
    }

    private final class SpinningQueueNotEmpty extends ConditionAbstractSpinning {

        @Override
        public boolean test() {
            return isEmpty();
        }
    }
}
