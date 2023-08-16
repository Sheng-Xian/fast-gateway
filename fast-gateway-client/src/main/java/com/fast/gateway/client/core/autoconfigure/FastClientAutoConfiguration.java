package com.fast.gateway.client.core.autoconfigure;

import com.fast.gateway.client.support.dubbo.Dubbo27ClientRegistryManager;
import com.fast.gateway.client.support.springmvc.SpringMVCClientRegistryManager;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * SpringBoot autoconfiguration loading class
 * @author sheng
 * @create 2023-07-22 22:54
 */
@Configuration
@EnableConfigurationProperties(FastProperties.class)
@ConditionalOnProperty(prefix = FastProperties.FAST_PREFIX, name = {"registryAddress", "namespace"})
public class FastClientAutoConfiguration {

    @Autowired
    private FastProperties fastProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegistryManager.class)
    public SpringMVCClientRegistryManager springMVCClientRegistryManager() throws Exception {
        return new SpringMVCClientRegistryManager(fastProperties);
    }

    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(Dubbo27ClientRegistryManager.class)
    public Dubbo27ClientRegistryManager dubboClientRegistryManager() throws Exception {
        return new Dubbo27ClientRegistryManager(fastProperties);
    }
}
