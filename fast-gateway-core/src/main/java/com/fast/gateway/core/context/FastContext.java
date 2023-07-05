package com.fast.gateway.core.context;

import com.fast.gateway.common.config.Rule;
import com.fast.gateway.common.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * @author sheng
 * @create 2023-07-04 21:46
 */
public class FastContext extends BasicContext{

    private final FastRequest fastRequest;

    private FastResponse fastResponse;

    private final Rule rule;

    private FastContext(String protocol, ChannelHandlerContext nettyContext, boolean keepAlive,
                       FastRequest fastRequest, Rule rule) {
        super(protocol, nettyContext, keepAlive);
        this.fastRequest = fastRequest;
        this.rule = rule;
    }

    public static class Builder {
        private String protocol;

        private ChannelHandlerContext nettyContext;

        private FastRequest fastRequest;

        private Rule rule;

        private boolean keepAlive;

        public Builder() {
        }

        public Builder setProtocol (String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyContext (ChannelHandlerContext nettyContext) {
            this.nettyContext = nettyContext;
            return this;
        }

        public Builder setFastRequest (FastRequest fastRequest) {
            this.fastRequest = fastRequest;
            return this;
        }

        public Builder setRule (Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive (boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public FastContext build() {
            AssertUtil.notNull(protocol, "protocol can't be null");
            AssertUtil.notNull(nettyContext, "nettyContext can't be null");
            AssertUtil.notNull(fastRequest, "fastRequest can't be null");
            AssertUtil.notNull(rule, "rule can't be null");
            return new FastContext(protocol, nettyContext, keepAlive, fastRequest, rule);
        }
    }

    public <T> T getRequiredAttributeKey(AttributeKey<T> key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute " + key + "is missing!");
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    public String getUniqueId() {
        return fastRequest.getUniqueId(); // serviceId : version
    }

    @Override
    public Rule getRule() {
        return this.rule;
    }

    @Override
    public FastRequest getRequest() {
        return this.fastRequest;
    }

    public FastRequest getOriginalRequest() {
        return this.fastRequest;
    }

    public FastRequest getMutableRequest() {
        return this.fastRequest;
    }

    @Override
    public FastResponse getResponse() {
        return this.fastResponse;
    }

    @Override
    public void setResponse(Object response) {
        this.fastResponse = (FastResponse) response;
    }

    @Override
    public void releaseRequest() {
        if (requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(fastRequest.getFullHttpRequest());
        }
    }
}
