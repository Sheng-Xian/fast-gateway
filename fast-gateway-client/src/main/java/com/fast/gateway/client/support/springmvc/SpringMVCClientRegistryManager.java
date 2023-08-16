package com.fast.gateway.client.support.springmvc;

import com.fast.gateway.client.core.AbstractClientRegistryManager;
import com.fast.gateway.client.core.FastAnnotationScanner;
import com.fast.gateway.client.core.autoconfigure.FastProperties;
import com.fast.gateway.common.config.Service;
import com.fast.gateway.common.config.ServiceInstance;
import com.fast.gateway.common.constants.BasicConst;
import com.fast.gateway.common.constants.FastConst;
import com.fast.gateway.common.util.NetUtils;
import com.fast.gateway.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SpringMVCClientRegistryManager
 * Http request registry manager
 * @author sheng
 * @create 2023-07-22 23:07
 */
@Slf4j
public class SpringMVCClientRegistryManager extends AbstractClientRegistryManager
        implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;

    private static final Set<Object> uniqueBeanSet = new HashSet<>();

    public SpringMVCClientRegistryManager(FastProperties fastProperties) throws Exception {
        super(fastProperties);
    }

    @PostConstruct
    private void init() {
        if (!ObjectUtils.allNotNull(serverProperties, serverProperties.getPort())) return;
        // Check if present validated properties is null, init
        super.whetherStart = true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (!whetherStart) return;
        if (applicationEvent instanceof WebServerInitializedEvent ||
                applicationEvent instanceof ServletWebServerInitializedEvent) {
            try {
                registrySpringMVC();
            } catch (Exception e) {
                log.error("#SpringMVCClientRegistryManager# register failed. ", e);
            }
        } else if (applicationEvent instanceof ApplicationStartedEvent) {
            System.err.println("********** Fast Spring MVC Started **********");
        }
    }

    /**
     * registrySpringMVC
     * Parse SpringMVC event, register
     */
    private void registrySpringMVC() throws Exception {
        Map<String, RequestMappingHandlerMapping> allRequestMapping =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class,
                true, false);
        for (RequestMappingHandlerMapping handlerMapping : allRequestMapping.values()) {
            Map<RequestMappingInfo, HandlerMethod> methodMap = handlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> method : methodMap.entrySet()) {
                HandlerMethod handlerMethod = method.getValue();
                Class<?> clazz = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(clazz); // SpringMVC Controller Bean Object
                // uniqueBeanSet to avoid reload the same bean
                if (uniqueBeanSet.add(bean)) {
                    Service service = FastAnnotationScanner.getInstance().scanBuilder(bean);
                    if (service != null) {
                        service.setEnvType(getEnv());
                        registerService(service);
                        ServiceInstance serviceInstance = new ServiceInstance();
                        String localIp = NetUtils.getLocalIp();
                        int port = serverProperties.getPort();
                        String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
                        String address = serviceInstanceId;
                        String uniqueId = service.getUniqueId();
                        String version = service.getVersion();

                        serviceInstance.setServiceInstanceId(serviceInstanceId);
                        serviceInstance.setUniqueId(uniqueId);
                        serviceInstance.setAddress(address);
                        serviceInstance.setWeight(FastConst.DEFAULT_WEIGHT);
                        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                        serviceInstance.setVersion(version);

                        registerServiceInstance(serviceInstance);
                    }
                }
            }
        }
    }
}
