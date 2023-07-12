package com.fast.gateway.common.exception;

import com.fast.gateway.common.enums.ResponseCode;

/**
 * @author sheng
 * @create 2023-07-10 13:05
 */
public class FastBaseException extends RuntimeException{
    private static final long serialVersionUID = 1818926105057012021L;

    public FastBaseException() {
    }

    protected ResponseCode code;

    public FastBaseException(String message, ResponseCode code) {
        super(message);
        this.code = code;
    }

    public FastBaseException(String message, Throwable cause, ResponseCode code) {
        super(message, cause);
        this.code = code;
    }

    public FastBaseException(Throwable cause, ResponseCode code) {
        super(cause);
        this.code = code;
    }

    public FastBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                             ResponseCode code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public ResponseCode getCode() {
        return code;
    }
}
