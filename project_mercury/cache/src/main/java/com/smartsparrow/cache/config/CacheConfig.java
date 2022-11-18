package com.smartsparrow.cache.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Settings for external caching provided by Redis
 */
public class CacheConfig {

    @JsonProperty("enableLearnerCache")
    private boolean enableLearnerCache;

    /**
     * Verify if CacheService should persist objects to external cache
     */
    public boolean isEnableLearnerCache() {
        return enableLearnerCache;
    }

    public CacheConfig setEnableLearnerCache(boolean enableLearnerCache) {
        this.enableLearnerCache = enableLearnerCache;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("enableLearnerCache", enableLearnerCache).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CacheConfig that = (CacheConfig) o;
        return enableLearnerCache == that.enableLearnerCache;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enableLearnerCache);
    }
}
