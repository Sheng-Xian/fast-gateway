package com.fast.gateway.core.context;

import com.fast.gateway.common.constants.BasicConst;
import com.fast.gateway.common.util.TimeUtil;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.cookie.Cookie;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author sheng
 * @create 2023-07-02 21:40
 * parse FullHttpRequest to FastRequest
 */
@Slf4j
public class FastRequest implements FastRequestMutable{

    /**
     * The parameter uniqueId must exit in header
     * serviceId:version
     */
    @Getter
    private final String uniqueId;

    @Getter
    private final long beginTime;

    @Getter
    private final Charset charset;

    // flow control, black list, white list
    @Getter
    private final String clientIp;

    // Request address: ip:host
    @Getter
    private final String host;

    // /xxx/xx/xxx
    @Getter
    private final String path;

    // uri: /xxx/xx/xxx?attr1=value1&attr2=value2
    @Getter
    private final String uri;

    // GET / POST/ PUT
    @Getter
    private final HttpMethod method;

    @Getter
    private final String contentType;

    @Getter
    private final HttpHeaders headers;

    @Getter
    private final QueryStringDecoder queryDecoder;

    @Getter
    private final FullHttpRequest fullHttpRequest;

    private String body;

    private Map<String, io.netty.handler.codec.http.cookie.Cookie> cookieMap;

    private Map<String, List<String>> postParameters;

    // mutable scheme, it's http:// by default
    private String modifyScheme;

    private String modifyHost;

    private String modifyPath;

    // build underlying request
    private final RequestBuilder requestBuilder;

    public FastRequest(String uniqueId, Charset charset, String clientIp, String host,
                       String uri, HttpMethod method, String contentType, HttpHeaders headers,
                       FullHttpRequest fullHttpRequest) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.uri = uri;
        this.queryDecoder = new QueryStringDecoder(uri, charset);
        this.path = queryDecoder.path();
        this.fullHttpRequest = fullHttpRequest;

        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder = new RequestBuilder();
        this.requestBuilder.setMethod(getMethod().name());
        this.requestBuilder.setHeaders(getHeaders());
        this.requestBuilder.setQueryParams(queryDecoder.parameters());
        ByteBuf contentBuffer = fullHttpRequest.content();
        if (Objects.nonNull(contentBuffer))
            this.requestBuilder.setBody(contentBuffer.nioBuffer());
    }

    @Override
    public void setModifyHost(String modifyHost) {
        this.modifyHost = modifyHost;
    }

    @Override
    public String getModifyHost() {
        return this.modifyHost;
    }

    @Override
    public void setModifyPath(String modifyPath) {
        this.modifyPath = modifyPath;
    }

    @Override
    public String getModifyPath() {
        return this.modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    public boolean isFormPost() {
        return HttpMethod.POST.equals(method) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    public boolean isJsonPost() {
        return HttpMethod.POST.equals(method) &&
                contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    @Override
    public void addFormParam(String name, String value) {
        if (isFormPost()) {
            requestBuilder.addFormParam(name, value);
        }
    }

    @Override
    public void setRequestTimeout(int requestTimeout) {
        requestBuilder.setRequestTimeout(requestTimeout);
    }

    public String getBody() {
        if (StringUtils.isEmpty(body)) {
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }

    public io.netty.handler.codec.http.cookie.Cookie getCookie(String name) {
        if (cookieMap == null) {
            cookieMap = new HashMap<>();
            String cookieStr = getHeaders().get(HttpHeaderNames.COOKIE);
            Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for (io.netty.handler.codec.http.cookie.Cookie cookie: cookies) {
                cookieMap.put(name, cookie);
            }
        }
        return cookieMap.get(name);
    }

    public List<String> getQueryParametersMultiple(String name) {
        return queryDecoder.parameters().get(name);
    }

    public List<String> getPostParametersMultiple(String name) {
        String body = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder paramDecoder = new QueryStringDecoder(body, false);
                postParameters = paramDecoder.parameters();
            }
            if (postParameters == null || postParameters.isEmpty()) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            try {
                return Lists.newArrayList(JsonPath.read(body, name).toString());
            } catch (Exception e) {
                log.error("#FastRequest# getPostParametersMultiple JsonPath parse failed, jsonPath: {}, body: {}, ",
                        name, body, e);
            }
        }
        return null;
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }
}
