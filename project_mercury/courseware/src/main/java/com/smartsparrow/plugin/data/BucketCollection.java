package com.smartsparrow.plugin.data;

import java.util.List;
import java.util.Objects;

/**
 * Wrapper class for holding a LogBucketInstance list.
 * The list will always contain two LogBucketInstance objects
 * (first is genereicLogStatemet, and the second will be either workspaceLogStatemet or learnspaceLogStatement, based on the provided pluginLogContext).
 */
public class BucketCollection {

    private List<LogBucketInstance> logBucketInstances;

    public List<LogBucketInstance> getLogBucketInstances() {
        return logBucketInstances;
    }

    public BucketCollection setLogBucketInstances(final List<LogBucketInstance> logBucketInstances) {
        this.logBucketInstances = logBucketInstances;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketCollection that = (BucketCollection) o;
        return Objects.equals(logBucketInstances, that.logBucketInstances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logBucketInstances);
    }

    @Override
    public String toString() {
        return "BucketCollection{" +
                "bucketList=" + logBucketInstances +
                '}';
    }
}
