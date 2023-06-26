package com.fast.gateway.common.constants;

/**
 * @author sheng
 * @create 2023-06-18 21:54
 */
public interface FastBufferHelper {

    String FLUSHER = "FLUSHER";

    String MPMC = "MPMC";

    static boolean isMpmc(String bufferType) {
        return MPMC.equals(bufferType);
    }

    static boolean isFlusher(String bufferType) {
        return FLUSHER.equals(bufferType);
    }
}
