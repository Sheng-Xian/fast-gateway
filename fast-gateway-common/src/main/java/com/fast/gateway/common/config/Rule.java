package com.fast.gateway.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author sheng
 * @create 2023-07-04 16:27
 */
@Data
public class Rule implements Comparable<Rule>, Serializable {

    private static final long serialVersionUID = -6679065404536306216L;

    private String id;

    private String name;

    private String protocol;

    // for a path with multiple rules to get the rule based on order
    private Integer order;

    // one rule with multiple filters
    private Set<Rule.FilterConfig> filterConfigs = new HashSet<>();

    public boolean addFilterConfig(Rule.FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }

    public Rule.FilterConfig getFilterConfig(String id) {
        for (Rule.FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getFilterId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    public boolean hasFilterId(String filterId) {
        for (Rule.FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getFilterId().equalsIgnoreCase(filterId)) return true;
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare = Integer.compare(getOrder(), o.getOrder());
        if (orderCompare == 0) {
            return getId().compareTo(o.getId());
        }
        return orderCompare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return id.equals(rule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class FilterConfig {
        private String filterId;

        // JSON String
        private String config;

        public String getFilterId() {
            return filterId;
        }

        public void setFilterId(String filterId) {
            this.filterId = filterId;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if ((o == null) || getClass() != o.getClass()) return false;
            FilterConfig filterConfig = (FilterConfig) o;
            return filterId.equals(filterConfig.filterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filterId);
        }
    }
}
