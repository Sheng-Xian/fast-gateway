package com.fast.gateway.core.netty.processor.filter;

import lombok.Data;

/**
 * The base class of all filter configuration implementation
 * @author sheng
 * @create 2023-07-21 17:02
 */
@Data
public class FilterConfig {
    private boolean loggable = false;
}
