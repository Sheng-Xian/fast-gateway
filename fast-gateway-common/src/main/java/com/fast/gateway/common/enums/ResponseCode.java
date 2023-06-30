package com.fast.gateway.common.enums;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

/**
 * @author sheng
 * @create 2023-06-27 23:52
 */
@Getter
public enum ResponseCode {
    SUCCESS(HttpResponseStatus.OK, 0,"Success"),

    INTERNAL_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 1000,"Internal Gateway Error"),

    SERVICE_UNAVAILABLE(HttpResponseStatus.SERVICE_UNAVAILABLE, 2000,"Service Unavailable, please try again later"),

    REQUEST_PARSE_ERROR(HttpResponseStatus.BAD_REQUEST, 10000,"Request Parsing Error, 'uniqueId' parameter must be present in the header"),

    REQUEST_PARSE_ERROR_NO_UNIQUEID(HttpResponseStatus.BAD_REQUEST, 10001,"Request Parsing Error, 'uniqueId' parameter must be present in the header"),

    PATH_NO_MATCHED(HttpResponseStatus.NOT_FOUND, 10002,"No matching path found, request failed quickly"),

    SERVICE_DEFINITION_NOT_FOUND(HttpResponseStatus.NOT_FOUND, 10003,"Service definition not found"),

    SERVICE_INVOKER_NOT_FOUND(HttpResponseStatus.NOT_FOUND, 10004,"Service invoker not found"),

    SERVICE_INSTANCE_NOT_FOUND(HttpResponseStatus.NOT_FOUND, 10005,"Service instance not found"),

    FILTER_CONFIG_PARSE_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 10006,"Filter configuration parsing error"),

    HTTP_RESPONSE_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 10030,"Service response error"),

    DUBBO_DISPATCH_CONFIG_EMPTY(HttpResponseStatus.INTERNAL_SERVER_ERROR, 10016,"Routing configuration cannot be empty"),

    DUBBO_PARAMETER_TYPE_EMPTY(HttpResponseStatus.BAD_REQUEST, 10017,"Request parameter type cannot be empty"),

    DUBBO_PARAMETER_VALUE_ERROR(HttpResponseStatus.BAD_REQUEST, 10018,"Request parameter parsing error"),

    DUBBO_METHOD_NOT_FOUNT(HttpResponseStatus.NOT_FOUND, 10021,"Method not found"),

    DUBBO_CONNECT_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 10022,"Downstream service error, please try again later"),

    DUBBO_REQUEST_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 10028,"Service request error"),

    DUBBO_RESPONSE_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 10029,"Service response error"),

    VERIFICATION_FAILED(HttpResponseStatus.BAD_REQUEST, 10030,"Request parameter verification failed"),

    BLACKLIST(HttpResponseStatus.FORBIDDEN, 10004,"Request IP is blacklisted"),

    WHITELIST(HttpResponseStatus.FORBIDDEN, 10005,"Request IP is not whitelisted")

            ;

    private HttpResponseStatus status;
    private int code;
    private String message;

    ResponseCode(HttpResponseStatus status, int code, String msg) {
        this.status = status;
        this.code = code;
        this.message = msg;
    }

}
