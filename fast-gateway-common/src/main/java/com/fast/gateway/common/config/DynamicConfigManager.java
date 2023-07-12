package com.fast.gateway.common.config;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sheng
 * @create 2023-07-10 23:20
 */
public class DynamicConfigManager {

    private ConcurrentHashMap<String /* uniqueId */, Service> serviceMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String /* uniqueId*/, Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String /* ruleId */, Rule> ruleMap = new ConcurrentHashMap<>();

    private DynamicConfigManager() {
    }

    private static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }

    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void putService(String uniqueId, Service service) {
        serviceMap.put(uniqueId, service);
    }

    public Service getService(String uniqueId) {
        return serviceMap.get(uniqueId);
    }

    public void removeService(String uniqueId) {
        serviceMap.remove(uniqueId);
    }

    public ConcurrentHashMap<String, Service> getServiceMap() {
        return serviceMap;
    }

    public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> servicesSet = serviceInstanceMap.get(uniqueId);
        servicesSet.add(serviceInstance);
    }

    public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> servicesSet = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> iterator = servicesSet.iterator();
        while (iterator.hasNext()) {
            ServiceInstance next = iterator.next();
            if (next.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                iterator.remove();
                break;
            }
        }
        servicesSet.add(serviceInstance);
    }

    public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
        Set<ServiceInstance> servicesSet = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> iterator = servicesSet.iterator();
        while (iterator.hasNext()) {
            ServiceInstance next = iterator.next();
            if (next.getServiceInstanceId().equals(serviceInstanceId)) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeServiceInstancesByUniqueId(String uniqueId) {
        serviceInstanceMap.remove(uniqueId);
    }

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    public Rule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    public ConcurrentHashMap<String, Rule> getRuleMap() {
        return ruleMap;
    }

}
