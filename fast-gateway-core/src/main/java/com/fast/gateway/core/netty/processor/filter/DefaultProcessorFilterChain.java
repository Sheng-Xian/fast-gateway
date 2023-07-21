package com.fast.gateway.core.netty.processor.filter;

import com.fast.gateway.core.context.Context;

/**
 * @author sheng
 * @create 2023-07-18 13:32
 */
public class DefaultProcessorFilterChain extends ProcessorFilterChain<Context>{

    private final String id;

    public DefaultProcessorFilterChain(String id) {
        this.id = id;
    }

    AbstractLinkedProcessorFilter<Context> dummyHead = new AbstractLinkedProcessorFilter<Context>() {
        @Override
        public boolean check(Context context) throws Throwable {
            return true;
        }

        @Override
        public void entry(Context context, Object... args) throws Throwable {
            super.fireNext(context, args);
        }
    };

    AbstractLinkedProcessorFilter<Context> end = dummyHead;

    @Override
    public boolean check(Context context) throws Throwable {
        return true;
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        dummyHead.transformEntry(context, args);
    }

    @Override
    public void addFirst(AbstractLinkedProcessorFilter<Context> filter) {
        filter.setNext(dummyHead.getNext());
        dummyHead.setNext(filter);
        if (end == dummyHead) end = filter;
    }

    @Override
    public void addLast(AbstractLinkedProcessorFilter<Context> filter) {
        end.setNext(filter);
        end = filter;
    }

    @Override
    public void setNext(AbstractLinkedProcessorFilter<Context> filter) {
        addLast(filter);
    }

    @Override
    public AbstractLinkedProcessorFilter<Context> getNext() {
        return dummyHead.getNext();
    }

    public String getId() {
        return id;
    }
}
