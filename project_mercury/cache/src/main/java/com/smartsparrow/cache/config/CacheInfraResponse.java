package com.smartsparrow.cache.config;

import java.util.Objects;

public class CacheInfraResponse {
    private String indexUri;
    private String applicationId;
    private boolean enabled;
    private Integer batchSize;
    private Integer completionTime;


    public String getIndexUri() {
        return indexUri;
    }

    public CacheInfraResponse setIndexUri(final String indexUri) {
        this.indexUri = indexUri;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public CacheInfraResponse setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CacheInfraResponse setEnabled(final boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public CacheInfraResponse setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public Integer getCompletionTime() {
        return completionTime;
    }

    public CacheInfraResponse setCompletionTime(final Integer completionTime) {
        this.completionTime = completionTime;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheInfraResponse that = (CacheInfraResponse) o;
        return enabled == that.enabled &&
                Objects.equals(indexUri, that.indexUri) &&
                Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(batchSize, that.batchSize) &&
                Objects.equals(completionTime, that.completionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexUri, applicationId, enabled, batchSize, completionTime);
    }

    @Override
    public String toString() {
        return "CacheInfraResponse{" +
                "indexUri='" + indexUri + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", enabled=" + enabled +
                ", batchSize=" + batchSize +
                ", completionTime=" + completionTime +
                '}';
    }
}
