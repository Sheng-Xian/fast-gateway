package com.fast.gateway.common.exception;

import com.fast.gateway.common.enums.ResponseCode;

/**
 * @author sheng
 * @create 2023-07-11 18:49
 */
public class FastPathNotMatchException extends FastBaseException {
    private static final long serialVersionUID = -8105041097949863820L;

    public FastPathNotMatchException() {
        this(ResponseCode.PATH_NO_MATCHED);
    }

    public FastPathNotMatchException(ResponseCode responseCode) {
        super(responseCode.getMessage(), responseCode);
    }

    public FastPathNotMatchException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
    }
}
