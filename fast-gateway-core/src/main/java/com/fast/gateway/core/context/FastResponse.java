package com.fast.gateway.core.context;

import com.fast.gateway.common.enums.ResponseCode;
import com.fast.gateway.common.util.JSONUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

/**
 * @author sheng
 * @create 2023-07-03 16:52
 */
@Data
public class FastResponse {

    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    // Response content
    private String content;

    private HttpResponseStatus httpResponseStatus;

    private Response futureResponse;

    private FastResponse() {
    }

    public void putHeader(CharSequence key, CharSequence value) {
        responseHeaders.add(key, value);
    }

    // build response by async httpclient response
    public static FastResponse buildFastResponse(Response futureResponse) {
        FastResponse fastResponse = new FastResponse();
        fastResponse.setFutureResponse(futureResponse);
        fastResponse.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return fastResponse;
    }

    // build response according to code especially for failed scenarios, send back message
    public static FastResponse buildFastResponse(ResponseCode code, Object... args) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, code.getStatus().code());
        objectNode.put(JSONUtil.CODE, code.getCode());
        objectNode.put(JSONUtil.MESSAGE, code.getMessage());
        FastResponse fastResponse = new FastResponse();
        fastResponse.setHttpResponseStatus(code.getStatus());
        fastResponse.putHeader(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        fastResponse.setContent(JSONUtil.toJSONString(objectNode));
        return fastResponse;
    }

    // for successful cases only, send back data
    public static FastResponse buildFastResponseByObject(Object data) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, data);
        FastResponse fastResponse = new FastResponse();
        fastResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        fastResponse.putHeader(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        fastResponse.setContent(JSONUtil.toJSONString(objectNode));
        return fastResponse;
    }
}
