package com.fast.gateway.common.config;

/**
 * @author sheng
 * @create 2023-07-10 16:26
 */
public interface ServiceInvoker {

    String getInvokerPath();

    void setInvokerPath(String invokerPath);

    String getRuleId();

    /**
     * get ruleId
     * @param ruleId - the only rule that service invoke, bind and execute
     */
    void setRuleId(String ruleId); // every method has its path and what rule applied

    int getTimeout();

    void setTimeout(int timeout);
}
