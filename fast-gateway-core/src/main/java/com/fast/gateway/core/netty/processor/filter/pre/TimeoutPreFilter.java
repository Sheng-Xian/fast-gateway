package com.fast.gateway.core.netty.processor.filter.pre;

import com.fast.gateway.common.config.DubboServiceInvoker;
import com.fast.gateway.common.config.ServiceInvoker;
import com.fast.gateway.common.constants.FastProtocol;
import com.fast.gateway.common.constants.ProcessorFilterConstants;
import com.fast.gateway.core.context.AttributeKey;
import com.fast.gateway.core.context.Context;
import com.fast.gateway.core.context.FastContext;
import com.fast.gateway.core.context.FastRequest;
import com.fast.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.fast.gateway.core.netty.processor.filter.Filter;
import com.fast.gateway.core.netty.processor.filter.FilterConfig;
import com.fast.gateway.core.netty.processor.filter.ProcessorFilterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sheng
 * @create 2023-07-21 16:57
 */
@Filter(
        id = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID,
        name = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_NAME,
        value = ProcessorFilterType.PRE,
        order = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ORDER
)
public class TimeoutPreFilter extends AbstractEntryProcessorFilter<TimeoutPreFilter.Config> {

    protected TimeoutPreFilter() {
        super(TimeoutPreFilter.Config.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {
            FastContext fastContext  = (FastContext) context;
            String protocol = fastContext.getProtocol();
            TimeoutPreFilter.Config config = (TimeoutPreFilter.Config) args[0];
            switch (protocol) {
                case FastProtocol.HTTP:
                    FastRequest fastRequest = fastContext.getRequest();
                    fastRequest.setRequestTimeout(config.getTimeout());
                    break;
                case FastProtocol.DUBBO:
                    DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker) fastContext.getRequiredAttributeKey(AttributeKey.DUBBO_INVOKER);
                    dubboServiceInvoker.setTimeout(config.getTimeout());
                    break;
            }
        } finally {
            super.fireNext(context, args);
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private Integer timeout;
    }
}
