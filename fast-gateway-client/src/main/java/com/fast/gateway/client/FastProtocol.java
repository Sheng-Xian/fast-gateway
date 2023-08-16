package com.fast.gateway.client;

import lombok.Getter;

/**
 * @author sheng
 * @create 2023-07-21 23:47
 */
public enum FastProtocol {

    HTTP("http", "http protocol"),
    DUBBO("dubbo", "");

    private String code;

    private String description;

    FastProtocol(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
