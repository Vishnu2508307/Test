package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.datastax.driver.core.LocalDate;

/**
 * This POJO is mainly used to track the bucket id from Cassandra
 * */
public class LogBucket {

    private LocalDate day;
    private long time;
    private String tableName;
    private UUID bucketId;

    public LocalDate getDay() {
        return day;
    }

    public LogBucket setDay(final LocalDate day) {
        this.day = day;
        return this;
    }

    public long getTime() {
        return time;
    }

    public LogBucket setTime(final long time) {
        this.time = time;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public LogBucket setTableName(final String tableName) {
        this.tableName = tableName;
        return this;
    }

    public UUID getBucketId() {
        return bucketId;
    }

    public LogBucket setBucketId(final UUID bucketId) {
        this.bucketId = bucketId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogBucket that = (LogBucket) o;
        return Objects.equals(day, that.day) &&
                Objects.equals(time, that.time) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(bucketId, that.bucketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, time, tableName, bucketId);
    }

    @Override
    public String toString() {
        return "LogBucket{" +
                "day=" + day +
                ", time=" + time +
                ", tableName='" + tableName + '\'' +
                ", bucketId=" + bucketId +
                '}';
    }
}
