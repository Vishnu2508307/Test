package com.smartsparrow.plugin.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.plugin.data.BucketRetentionPolicy;

/**
 * This class holds the config for the log buckets
 * */
public class BucketConfig {
    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("maxRecordCount")
    private Long maxRecordCount;

    @JsonProperty("retentionPolicy")
    private BucketRetentionPolicy retentionPolicy;

    @JsonProperty("logBucketInstances")
    private Long logBucketInstances;

    public String getTableName() {
        return tableName;
    }

    public BucketConfig setTableName(final String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Long getMaxRecordCount() {
        return maxRecordCount;
    }

    public BucketConfig setMaxRecordCount(final Long maxRecordCount) {
        this.maxRecordCount = maxRecordCount;
        return this;
    }

    public BucketRetentionPolicy getRetentionPolicy() {
        return retentionPolicy;
    }

    public BucketConfig setRetentionPolicy(final BucketRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    public Long getLogBucketInstances() {
        return logBucketInstances;
    }

    public BucketConfig setLogBucketInstances(final Long logBucketInstances) {
        this.logBucketInstances = logBucketInstances;
        return this;
    }

    @Override
    public String toString() {
        return "BucketConfig{" +
                "tableName='" + tableName + '\'' +
                ", maxRecordCount=" + maxRecordCount +
                ", retentionPolicy=" + retentionPolicy +
                ", logBucketInstances=" + logBucketInstances +
                '}';
    }
}
