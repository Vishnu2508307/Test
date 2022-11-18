package com.smartsparrow.plugin.wiring;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginLogConfig {

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty(value = "bucketConfigs")
    private List<BucketConfig> bucketConfigs;

    /**
     * A toggle for enabling/disabling persisting client side logging into Cassandra.
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public PluginLogConfig setEnabled(final Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * A list of bucket configs for different tables
     */
    public List<BucketConfig> getBucketConfigs() {
        return bucketConfigs;
    }

    public PluginLogConfig setBucketConfigs(final List<BucketConfig> bucketConfigs) {
        this.bucketConfigs = bucketConfigs;
        return this;
    }

    @Override
    public String toString() {
        return "PluginLogConfig{" +
                "enabled=" + enabled +
                ", bucketConfigs=" + bucketConfigs +
                '}';
    }
}
