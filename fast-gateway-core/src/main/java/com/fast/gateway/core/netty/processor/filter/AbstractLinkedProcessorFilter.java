package com.fast.gateway.core.netty.processor.filter;

import com.fast.gateway.core.context.Context;

/**
 * abstract filters in form of linked list
 * @author sheng
 * @create 2023-07-16 13:51
 */
public abstract class AbstractLinkedProcessorFilter<T> implements ProcessorFilter<Context>{
    protected AbstractLinkedProcessorFilter<T> next = null;

    @Override
    public void fireNext(Context context, Object... args) throws Throwable {
        if (next != null) {
            if (!next.check(context)) {
                next.fireNext(context, args);
            } else {
                next.transformEntry(context, args);
            }
        } else {
            return;
        }
    }

    @Override
    public void transformEntry(Context t, Object... args) throws Throwable {
        // Sub class call to operate next node(element)
        entry(t, args);
    }

    public void setNext(AbstractLinkedProcessorFilter<T> next) {
        this.next = next;
    }

    public AbstractLinkedProcessorFilter<T> getNext() {
        return this.next;
    }
}
