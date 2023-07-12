package com.fast.gateway.common.constants;

/**
 * @author sheng
 * @create 2023-07-12 0:18
 */
public interface FastProtocol {
    String HTTP = "http";

    String DUBBO = "dubbo";

    static boolean isHttp(String protocol) {
        return HTTP.equals(protocol);
    }

    static boolean isDubbo(String protocol) {
        return DUBBO.equals(protocol);
    }
}
