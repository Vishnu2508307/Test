package com.smartsparrow.plugin.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class for holding a cache map. The key is the table name, and the value is a list of LogBucketInstance.
 * This class is meant to be set/get to Redis bucket.
 */
public class LogBucketInstanceCache implements Serializable {
    private static final long serialVersionUID = -2204308350431132882L;

    private Map<String, CopyOnWriteArrayList<LogBucketInstance>> cache;

    public Map<String, CopyOnWriteArrayList<LogBucketInstance>> getCache() {
        return cache;
    }

    public LogBucketInstanceCache setCache(final Map<String, CopyOnWriteArrayList<LogBucketInstance>> cache) {
        this.cache = cache;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogBucketInstanceCache that = (LogBucketInstanceCache) o;
        return Objects.equals(cache, that.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cache);
    }

    @Override
    public String toString() {
        return "LogBucketInstanceCache{" +
                "cache=" + cache +
                '}';
    }
}
