package com.fast.gateway.core.helper;

import com.fast.gateway.common.enums.ResponseCode;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

/**
 * @author sheng
 * @create 2023-06-27 23:50
 */
public class ResponseHelper {
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        String errorContent = "Response Internal Error";
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(errorContent.getBytes()));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, errorContent.length());
        return httpResponse;
    }
}
