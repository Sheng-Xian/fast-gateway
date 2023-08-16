package com.fast.gateway.client.core;
import com.fast.gateway.client.FastInvoker;
import com.fast.gateway.client.FastProtocol;
import com.fast.gateway.client.FastService;
import com.fast.gateway.client.support.dubbo.DubboConstants;
import com.fast.gateway.common.config.DubboServiceInvoker;
import com.fast.gateway.common.config.HttpServiceInvoker;
import com.fast.gateway.common.config.Service;
import com.fast.gateway.common.config.ServiceInvoker;
import com.fast.gateway.common.constants.BasicConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Annotation scanner class, use to scan all users define @FastService and @FastInvoker
 * @author sheng
 * @create 2023-07-22 16:21
 */
public class FastAnnotationScanner {
    private FastAnnotationScanner() {
    }

    private static class SingletonHolder {
        static final FastAnnotationScanner INSTANCE = new FastAnnotationScanner();
    }

    public static  FastAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Scan the incoming bean object and finally return a Service
     * @param bean - Bean Object
     * @param args - additional parameters, such as ServiceBean which is needed for registering Dubbo
     * @return Service
     */
    public synchronized Service scanBuilder(Object bean, Object... args) {
        Class<?> clazz = bean.getClass();
        boolean isPresent = clazz.isAnnotationPresent(FastService.class);
        if (isPresent) {
            FastService fastService = clazz.getAnnotation(FastService.class);
            String serviceId = fastService.serviceId();
            FastProtocol protocol = fastService.protocol();
            String patternPath = fastService.patternPath();
            String version = fastService.version();
            Service service = new Service();
            HashMap<String, ServiceInvoker> invokerMap = new HashMap<>();

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                FastInvoker fastInvoker = method.getAnnotation(FastInvoker.class);
                if (fastInvoker == null) {
                    continue;
                }
                String path = fastInvoker.path();
                switch (protocol) {
                    case HTTP:
                        HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path, bean, method);
                        invokerMap.put(path, httpServiceInvoker);
                        break;
                    case DUBBO:
                        ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                        DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                        // dubbo version reset for service version
                        String dubboVersion = dubboServiceInvoker.getVersion();
                        if (!StringUtils.isBlank(dubboVersion)) {
                            version = dubboVersion;
                        }
                        invokerMap.put(path, dubboServiceInvoker);
                        break;
                    default:
                        break;
                }
            }
            service.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
            service.setServiceId(serviceId);
            service.setVersion(version);
            service.setProtocol(protocol.getCode());
            service.setPatternPath(patternPath);
            service.setEnable(true);
            service.setInvokerMap(invokerMap);
            return service;
        }
        return null;
    }

    /**
     * create HttpServiceInvoker
     * @param path - invoke path
     * @param bean - Bean Object
     * @param method - invoker method
     * @return HttpServiceInvoker
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path, Object bean, Method method) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    // TODO: Because no dubbo correspond ServiceBean, no related methods could get

    /**
     * create DubboServiceInvoker
     * @param path - String
     * @param serviceBean - ServiceBean<?>
     * @param method - Method
     * @return DubboServiceInvoker
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);
        String methodName = method.getName();
        String registryAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setRegisterAddress(registryAddress);
        dubboServiceInvoker.setMethod(methodName);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);

        Integer serviceTimeout = serviceBean.getTimeout();
        if (serviceTimeout == null || serviceTimeout.intValue() == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if (providerConfig != null) {
                Integer providerConfigTimeout = providerConfig.getTimeout();
                if (providerConfigTimeout == null || providerConfigTimeout.intValue() == 0) {
                    serviceTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    serviceTimeout = providerConfigTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(serviceTimeout);

        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);

        return dubboServiceInvoker;
    }
}