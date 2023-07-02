package com.fast.gateway.core.context;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * @author sheng
 * @create 2023-07-02 13:52
 */
public interface FastRequestMutable {

    void setModifyHost(String host);

    String getModifyHost();

    void setModifyPath(String path);

    String getModifyPath();

    void addHeader(CharSequence name, String value);

    void setHeader(CharSequence name, String value);

    void addQueryParam(String name, String value);

    // using cookie to transfer to underlying services, so need to use AsyncHttpHelper's cookie
    void addOrReplaceCookie(Cookie cookie);

    void addFormParam(String name, String value);

    void setRequestTimeout(int requestTimeout);

    // build the request object to forward the request
    Request build();

    String getFinalUrl();
}
