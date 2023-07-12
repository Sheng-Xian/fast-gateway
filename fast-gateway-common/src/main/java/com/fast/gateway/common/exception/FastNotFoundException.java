package com.fast.gateway.common.exception;

import com.fast.gateway.common.enums.ResponseCode;

/**
 * Service information is not found, for example: service, service instance etc.
 * @author sheng
 * @create 2023-07-11 12:18
 */
public class FastNotFoundException extends FastBaseException{
    private static final long serialVersionUID = 7172515578835792379L;

    public FastNotFoundException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public FastNotFoundException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
    }
}
