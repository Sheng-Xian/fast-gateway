package com.fast.gateway.common.config;

import java.io.Serializable;
import java.util.Objects;

/**
 * One service corresponds multiple instances.
 * @author sheng
 * @create 2023-07-10 22:47
 */
public class ServiceInstance implements Serializable {
    private static final long serialVersionUID = 6123566681173741395L;

    /**
     * service instanceId: ip:port
     */
    private String serviceInstanceId;

    private String uniqueId;

    /**
     * service instance address: ip:port
     */
    private String address;

    private String tags;

    private Integer weight;

    /**
     * service register timestamp: for load balance, warm up
     */
    private long registerTime;

    private boolean enable = true;

    private String version;

    public ServiceInstance() {
    }

    public ServiceInstance(String serviceInstanceId, String uniqueId, String address, String tags,
                           Integer weight, long registerTime, boolean enable, String version) {
        this.serviceInstanceId = serviceInstanceId;
        this.uniqueId = uniqueId;
        this.address = address;
        this.tags = tags;
        this.weight = weight;
        this.registerTime = registerTime;
        this.enable = enable;
        this.version = version;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return Objects.equals(serviceInstanceId, that.serviceInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId);
    }
}
