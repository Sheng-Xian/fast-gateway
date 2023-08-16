package com.fast.gateway.client.support.dubbo;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * Dubbo 2.7.x client registry manager
 * @author sheng
 * @create 2023-07-22 23:08
 */
@Slf4j
public class Dubbo27ClientRegistryManager extends AbstractClientRegistryManager
        implements EnvironmentAware, ApplicationListener<ApplicationEvent> {

    private Environment environment;

    private static final Set<Object> uniqueBeanSet = new HashSet<>();

    public Dubbo27ClientRegistryManager(FastProperties fastProperties) throws Exception {
        super(fastProperties);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    private void init() {
        String port = environment.getProperty(DubboConstants.DUBBO_PROTOCOL_PORT);
        if (StringUtils.isEmpty(port)) {
            log.error("Fast Dubbo service didn't start yet.");
            return;
        }
        whetherStart = true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (!whetherStart) {
            return;
        }
        if (applicationEvent instanceof ServiceBeanExportedEvent) {
            ServiceBean<?> serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();
            try {
                registryServiceBean(serviceBean);
            } catch (Exception e) {
                log.error("Fast Dubbo register ServiceBean failed, ServiceBean = {}", serviceBean, e);
            }
        } else if (applicationEvent instanceof ApplicationStartedEvent) {
            System.err.println("********** Fast Dubbo Started **********");
        }
    }

    /**
     * Register Dubbo service, from ServiceBeanExportedEvent get ServiceBean object
     * @param serviceBean - ServiceBean
     */
    private void registryServiceBean(ServiceBean<?> serviceBean) throws Exception {
        Object bean = serviceBean.getRef();
        if (uniqueBeanSet.add(bean)) {
            Service service = FastAnnotationScanner.getInstance().scanBuilder(bean, serviceBean);
            if (service != null) {
                service.setEnvType(getEnv());
                registerService(service);
                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtils.getLocalIp();
                int port = serviceBean.getProtocol().getPort();
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
