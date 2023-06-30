package com.fast.gateway.common.concurrent.queue.mpmc;

/**
 * @author sheng
 * @create 2023-06-28 18:30
 */
public interface ConcurrentQueue<E> {
    boolean offer(E e);
    E poll();
    E peek();
    int size();
    int capacity();
    boolean isEmpty();
    boolean contains(Object o);
    int remove(E[] e);
    void clear();
}
