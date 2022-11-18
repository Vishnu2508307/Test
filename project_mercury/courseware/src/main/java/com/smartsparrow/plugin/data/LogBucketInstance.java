package com.smartsparrow.plugin.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Class for holding a LogBucket data, plus some related BucketConfig data.
 */
public class LogBucketInstance extends LogBucket implements Serializable {

    private static final long serialVersionUID = -2834934266725760771L;

    private Long maxRecordCount;
    private Long currentCount;
    private BucketRetentionPolicy retentionPolicy;
    private String dayAsString;

    public Long getMaxRecordCount() {
        return maxRecordCount;
    }

    public LogBucketInstance setMaxRecordCount(final Long maxRecordCount) {
        this.maxRecordCount = maxRecordCount;
        return this;
    }

    public Long getCurrentCount() {
        return currentCount;
    }

    public LogBucketInstance setCurrentCount(final Long currentCount) {
        this.currentCount = currentCount;
        return this;
    }

    public BucketRetentionPolicy getRetentionPolicy() {
        return retentionPolicy;
    }

    public LogBucketInstance setRetentionPolicy(final BucketRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    public String getDayAsString() {
        return dayAsString;
    }

    /**
     * com.datastax.driver.core.LocalDate does not implement Serializable, thus could not be set to Redis.
     * This method is is to set com.datastax.driver.core.LocalDate.toString() value in yyyy-mm-dd format.
     */
    public LogBucketInstance setDayAsString(final String dayAsString) {
        this.dayAsString = dayAsString;
        return this;
    }

    @Override
    public LogBucketInstance setTime(final long time) {
        super.setTime(time);
        return this;
    }

    @Override
    public LogBucketInstance setTableName(final String tableName) {
        super.setTableName(tableName);
        return this;
    }

    @Override
    public LogBucketInstance setBucketId(final UUID bucketId) {
        super.setBucketId(bucketId);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LogBucketInstance that = (LogBucketInstance) o;
        return Objects.equals(maxRecordCount, that.maxRecordCount) &&
                Objects.equals(currentCount, that.currentCount) &&
                retentionPolicy == that.retentionPolicy &&
                Objects.equals(dayAsString, that.dayAsString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxRecordCount, currentCount, retentionPolicy, dayAsString);
    }

    @Override
    public String toString() {
        return "LogBucketInstance{" +
                "maxRecordCount=" + maxRecordCount +
                ", currentCount=" + currentCount +
                ", retentionPolicy=" + retentionPolicy +
                ", dayAsString='" + dayAsString + '\'' +
                '}';
    }
}
