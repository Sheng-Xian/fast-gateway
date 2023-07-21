package com.fast.gateway.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author sheng
 * @create 2023-07-10 16:07
 */
@Data
public class Service implements Serializable {

    private static final long serialVersionUID = -1433648875048312939L;

    private String uniqueId;

    private String serviceId;

    private String version;

    private String protocol; // http (mvc,http)

    // ANT expression
    private String patternPath;

    // DEV/SIT/PROD
    private String envType;

    private boolean enable = true; // instances could be disabled, too.

    //interfaces/methods of service
    private Map<String /* invokerPath */, ServiceInvoker /* method description */> invokerMap;

    public Service() {
    }

    public Service(String uniqueId, String serviceId, String version, String protocol, String patternPath, String envType, boolean enable, Map<String, ServiceInvoker> invokerMap) {
        this.uniqueId = uniqueId;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.patternPath = patternPath;
        this.envType = envType;
        this.enable = enable;
        this.invokerMap = invokerMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service that = (Service) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
