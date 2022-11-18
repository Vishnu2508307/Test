package com.smartsparrow.la.config;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PLPFirehoseConfig {
    @JsonProperty("queueName")
    private String queueName;
    @JsonProperty("queueOwnerAWSAccountId")
    private String queueOwnerAWSAccountId;
    @JsonProperty("region")
    private String region;
    @JsonProperty("enabled")
    private boolean enabled;

    public String getQueueName() {
        return queueName;
    }

    public String getQueueOwnerAWSAccountId() {
        return queueOwnerAWSAccountId;
    }

    public String getRegion() {
        return region;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Build SQS name URL
     * @return String of queue URL
     */
    public String getSQSURL() {
        return getQueueName() +
                "?queueOwnerAWSAccountId=" +
                getQueueOwnerAWSAccountId() +
                "&region=" + Regions.fromName(getRegion()).name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PLPFirehoseConfig that = (PLPFirehoseConfig) o;
        return enabled == that.enabled &&
                Objects.equals(queueName, that.queueName) &&
                Objects.equals(queueOwnerAWSAccountId, that.queueOwnerAWSAccountId) &&
                Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueName, queueOwnerAWSAccountId, region, enabled);
    }

    @Override
    public String toString() {
        return "PLPFirehoseConfig{" +
                "queueName='" + queueName + '\'' +
                ", queueOwnerAWSAccountId='" + queueOwnerAWSAccountId + '\'' +
                ", region='" + region + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
