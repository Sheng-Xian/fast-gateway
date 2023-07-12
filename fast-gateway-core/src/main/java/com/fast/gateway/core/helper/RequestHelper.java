package com.fast.gateway.core.helper;

import com.fast.gateway.common.config.DynamicConfigManager;
import com.fast.gateway.common.config.Rule;
import com.fast.gateway.common.config.Service;
import com.fast.gateway.common.config.ServiceInvoker;
import com.fast.gateway.common.constants.BasicConst;
import com.fast.gateway.common.constants.FastConst;
import com.fast.gateway.common.constants.FastProtocol;
import com.fast.gateway.common.enums.ResponseCode;
import com.fast.gateway.common.exception.FastNotFoundException;
import com.fast.gateway.common.exception.FastPathNotMatchException;
import com.fast.gateway.common.exception.FastResponseException;
import com.fast.gateway.common.util.AntPathMatcher;
import com.fast.gateway.core.context.AttributeKey;
import com.fast.gateway.core.context.FastContext;
import com.fast.gateway.core.context.FastRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author sheng
 * @create 2023-07-04 22:53
 */
public class RequestHelper {
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * Parse request information, build Context object
     *
     * @return FastContext
     */
    public static FastContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
        // 1. Build request object
        FastRequest fastRequest = doRequest(request, ctx);
        // 2. According to request object uniqueId, obtain resources services information
        Service service = getService(fastRequest);
        // 3. Match path failed strategy
        if (!ANT_PATH_MATCHER.match(service.getPatternPath(), fastRequest.getPath())) {
            throw new FastPathNotMatchException();
        }
        // 4. According request object, obtain object correspond method invoker and rule
        ServiceInvoker serviceInvoker = getServiceInvoker(fastRequest, service);
        String ruleId = serviceInvoker.getRuleId();
        Rule rule = DynamicConfigManager.getInstance().getRule(ruleId);
        // 5. Build FastContext Object
        FastContext fastContext = new FastContext.Builder().setProtocol(service.getProtocol()).setFastRequest(fastRequest).setNettyContext(ctx).setKeepAlive(HttpUtil.isKeepAlive(request)).setRule(rule).build();
        // 6. Set necessary context parameter
        putContext(fastContext, serviceInvoker);
        return fastContext;
    }

    /**
     * Build FastRequest object
     *
     * @param request - FullHttpRequest
     * @param ctx     - ChannelHandlerContext
     * @return FastRequest
     */
    private static FastRequest doRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        HttpHeaders headers = request.headers();
        String uniqueId = headers.get(FastConst.UNIQUE_ID);

        if (StringUtils.isBlank(uniqueId)) {
            throw new FastResponseException(ResponseCode.REQUEST_PARSE_ERROR_NO_UNIQUEID);
        }

        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = request.method();
        String uri = request.uri();
        String clientIp = getClientIp(ctx, request);
        String contentType = HttpUtil.getMimeType(request) == null ? null : HttpUtil.getMimeType(request).toString();
        Charset charset = HttpUtil.getCharset(request, StandardCharsets.UTF_8);

        return new FastRequest(uniqueId, charset, clientIp, host, uri, method, contentType, headers, request);
    }

    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;
        if (StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            if (values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if (clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }

    /**
     * According to request get service resource information
     *
     * @param fastRequest - incoming fastRequest
     * @return Service
     */
    private static Service getService(FastRequest fastRequest) {
        Service service = DynamicConfigManager.getInstance().getService(fastRequest.getUniqueId());
        if (service == null) {
            throw new FastNotFoundException(ResponseCode.SERVICE_NOT_FOUND);
        }
        return service;
    }

    private static ServiceInvoker getServiceInvoker(FastRequest fastRequest, Service service) {
        Map<String, ServiceInvoker> invokerMap = service.getInvokerMap();
        ServiceInvoker serviceInvoker = invokerMap.get(fastRequest.getPath());
        if (serviceInvoker == null) {
            throw new FastNotFoundException(ResponseCode.SERVICE_INVOKER_NOT_FOUND);
        }
        return serviceInvoker;
    }

    private static void putContext(FastContext fastContext, ServiceInvoker serviceInvoker) {
        switch (fastContext.getProtocol()) {
            case FastProtocol.HTTP:
                fastContext.putAttribute(AttributeKey.HTTP_INVOKER, serviceInvoker);
                break;
            case FastProtocol.DUBBO:
                fastContext.putAttribute(AttributeKey.DUBBO_INVOKER, serviceInvoker);
                break;
            default:
                break;
        }
    }
}
