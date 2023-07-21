package com.fast.gateway.core.netty.processor.filter;

/**
 * @author sheng
 * @create 2023-07-16 12:55
 */
public enum ProcessorFilterType {
    PRE("PRE", "Pre processor"),
    ROUTE("ROUTE", "Route processor"),
    ERROR("ERROR", "Error processor"),
    POST("POST", "Post processor");

    private final String code;

    private final String message;

    ProcessorFilterType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
