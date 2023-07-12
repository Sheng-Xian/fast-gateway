package com.fast.gateway.common.exception;

import com.fast.gateway.common.enums.ResponseCode;

/**
 * @author sheng
 * @create 2023-07-10 13:05
 */
public class FastResponseException extends FastBaseException {

    private static final long serialVersionUID = 8023952902926038430L;

    public FastResponseException() {
        this(ResponseCode.INTERNAL_ERROR);
    }

    public FastResponseException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public FastResponseException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.code = code;
    }
}
