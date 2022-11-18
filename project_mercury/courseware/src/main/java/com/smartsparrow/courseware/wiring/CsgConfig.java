package com.smartsparrow.courseware.wiring;

import java.util.Objects;


public class CsgConfig {
    private String indexUri;
    private String applicationId;
    private boolean enabled;
    private Integer batchSize;
    private Integer completionTime;


    public String getIndexUri() {
        return indexUri;
    }

    public CsgConfig setIndexUri(final String indexUri) {
        this.indexUri = indexUri;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public CsgConfig setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CsgConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public CsgConfig setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public Integer getCompletionTime() {
        return completionTime;
    }

    public CsgConfig setCompletionTime(final Integer completionTime) {
        this.completionTime = completionTime;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CsgConfig csgConfig = (CsgConfig) o;
        return enabled == csgConfig.enabled &&
                Objects.equals(batchSize, csgConfig.batchSize) &&
                Objects.equals(completionTime, csgConfig.completionTime) &&
                Objects.equals(indexUri, csgConfig.indexUri) &&
                Objects.equals(applicationId, csgConfig.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchSize, completionTime, indexUri, applicationId, enabled);
    }

    @Override
    public String toString() {
        return "CsgConfig{" +
                "batchSize='" + batchSize + '\'' +
                ", completionTime='" + completionTime + '\'' +
                ", indexUri='" + indexUri + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
