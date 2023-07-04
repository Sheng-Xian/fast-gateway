package com.fast.gateway.core.helper;

import com.fast.gateway.common.enums.ResponseCode;
import com.fast.gateway.core.context.FastResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

/**
 * @author sheng
 * @create 2023-06-27 23:50
 */
public class ResponseHelper {
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        FastResponse fastResponse = FastResponse.buildFastResponse(responseCode);

        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(fastResponse.getContent().getBytes()));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        return httpResponse;
    }
}
