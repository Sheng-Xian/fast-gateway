package com.fast.gateway.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author sheng
 * @create 2023-07-01 11:11
 */
public abstract class AttributeKey<T> {
    private static final Map<String, AttributeKey<?>> namedMap = new HashMap<>();
    public static final AttributeKey<Set<String>> MATCH_ADDRESS = create(Set.class);

    static {
        namedMap.put("MATCH_ADDRESS", MATCH_ADDRESS);
    }

    public abstract T cast(Object value);

    public static AttributeKey<?> valueOf(String name) {
        return namedMap.get(name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> AttributeKey<T> create(final Class<? super T> valueClass) {
        return new SimpleAttributeKey(valueClass);
    }

    public static class SimpleAttributeKey<T> extends AttributeKey<T> {

        private final Class<T> valueClass;

        SimpleAttributeKey(final Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public T cast(Object value) {
            return valueClass.cast(value);
        }

        @Override
        public String toString() {
            if (valueClass != null) {
                StringBuilder sb = new StringBuilder(getClass().getName());
                sb.append("<");
                sb.append(valueClass.getName());
                sb.append(">");
                return sb.toString();
            }
            return super.toString();
        }
    }
}
