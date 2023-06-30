package com.fast.gateway.common.concurrent.queue.mpmc;

/**
 * @author sheng
 * @create 2023-06-28 18:32
 */
public class MpmcConcurrentQueue<E> implements ConcurrentQueue<E> {
    protected final int size;
    final long mask;
    final Cell<E>[] buffer;
    final ContendedAtomicLong head = new ContendedAtomicLong(0L);
    final ContendedAtomicLong tail = new ContendedAtomicLong(0L);

    @SuppressWarnings("unchecked")
    public MpmcConcurrentQueue(final int capacity) {
        int c = 2;
        while (c < capacity) {
            c <<= 1;
        }
        size = c;
        mask = size - 1L;
        buffer = new Cell[size];
        for (int i = 0; i < size; i++) {
            buffer[i] = new Cell<>(i);
        }
    }

    @Override
    public boolean offer(E e) {
        Cell<E> cell;
        long tail = this.tail.get();
        for (;;) {
            cell = buffer[(int)(tail & mask)];
            final long seq = cell.seq.get();
            final long dif = seq - tail;
            if (dif == 0) {
                if (this.tail.compareAndSet(tail, tail + 1)) {
                    break;
                }
            } else if (dif < 0) {
                return false;
            } else {
                tail = this.tail.get();
            }
        }
        cell.entry = e;
        cell.seq.set(tail + 1);
        return true;
    }

    @Override
    public E poll() {
        Cell<E> cell;
        long head = this.head.get();
        for (;;) {
            cell = buffer[(int)(head & mask)];
            long seq = cell.seq.get();
            final long dif = seq - (head + 1L);
            if (dif == 0) {
                if (this.head.compareAndSet(head, head + 1)) {
                    break;
                }
            } else if (dif < 0) {
                return null;
            } else {
                head = this.head.get();
            }
        }

        try {
            return cell.entry;
        } finally {
            cell.entry = null;
            cell.seq.set(head + mask + 1L);
        }
    }

    @Override
    public E peek() {
        return buffer[(int)(head.get() & mask)].entry;
    }

    @Override
    public int size() {
        return (int)Math.max((tail.get() - head.get()), 0);
    }

    @Override
    public int capacity() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return head.get() == tail.get();
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < size(); i++) {
            final int slot = (int)((head.get() + i) & mask);
            if (buffer[slot].entry != null && buffer[slot].entry.equals(o)) return true;
        }
        return false;
    }

    @Override
    // drain the whole queue at once
    public int remove(final E[] e) {
        int nRead = 0;
        while (nRead < e.length && !isEmpty()) {
            final E entry = poll();
            if (entry != null) {
                e[nRead++] = entry;
            }
        }
        return nRead;
    }

    @Override
    public void clear() {
        while (isEmpty()) {
            poll();
        }
    }

    protected static final class Cell<R> {
        final ContendedAtomicLong seq = new ContendedAtomicLong(0L);

        R entry;

        Cell(final long s) {
            seq.set(s);
            entry = null;
        }
    }
}
