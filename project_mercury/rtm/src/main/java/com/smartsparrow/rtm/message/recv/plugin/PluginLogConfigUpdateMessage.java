package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;

import com.smartsparrow.plugin.data.BucketRetentionPolicy;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PluginLogConfigUpdateMessage extends ReceivedMessage {

    private String tableName;
    private Boolean enabled;
    private Long maxRecordCount;
    private BucketRetentionPolicy retentionPolicy;
    private Long logBucketInstances;

    public String getTableName() {
        return tableName;
    }

    public PluginLogConfigUpdateMessage setTableName(final String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public PluginLogConfigUpdateMessage setEnabled(final Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Long getMaxRecordCount() {
        return maxRecordCount;
    }

    public PluginLogConfigUpdateMessage setMaxRecordCount(final Long maxRecordCount) {
        this.maxRecordCount = maxRecordCount;
        return this;
    }

    public BucketRetentionPolicy getRetentionPolicy() {
        return retentionPolicy;
    }

    public PluginLogConfigUpdateMessage setRetentionPolicy(final BucketRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    public Long getLogBucketInstances() {
        return logBucketInstances;
    }

    public PluginLogConfigUpdateMessage setLogBucketInstances(final Long logBucketInstances) {
        this.logBucketInstances = logBucketInstances;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginLogConfigUpdateMessage that = (PluginLogConfigUpdateMessage) o;
        return Objects.equals(tableName, that.tableName) &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(maxRecordCount, that.maxRecordCount) &&
                retentionPolicy == that.retentionPolicy &&
                Objects.equals(logBucketInstances, that.logBucketInstances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, enabled, maxRecordCount, retentionPolicy, logBucketInstances);
    }

    @Override
    public String toString() {
        return "PluginLogConfigUpdateMessage{" +
                "tableName='" + tableName + '\'' +
                ", enabled=" + enabled +
                ", maxRecordCount=" + maxRecordCount +
                ", retentionPolicy=" + retentionPolicy +
                ", logBucketInstances=" + logBucketInstances +
                '}';
    }
}
