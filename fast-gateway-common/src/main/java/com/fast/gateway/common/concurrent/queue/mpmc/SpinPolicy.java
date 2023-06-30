package com.fast.gateway.common.concurrent.queue.mpmc;

/**
 * @author sheng
 * @create 2023-06-29 0:43
 */
public enum SpinPolicy {
    WAITING,
    BLOCKING,
    SPINNING
}
