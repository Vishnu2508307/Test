package com.smartsparrow.courseware.wiring;

import java.util.Objects;


public class CsgInfraResponse {
    private String indexUri;
    private String applicationId;
    private boolean enabled;
    private Integer batchSize;
    private Integer completionTime;


    public String getIndexUri() {
        return indexUri;
    }

    public CsgInfraResponse setIndexUri(final String indexUri) {
        this.indexUri = indexUri;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public CsgInfraResponse setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CsgInfraResponse setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public CsgInfraResponse setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public Integer getCompletionTime() {
        return completionTime;
    }

    public CsgInfraResponse setCompletionTime(final Integer completionTime) {
        this.completionTime = completionTime;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CsgInfraResponse csgConfig = (CsgInfraResponse) o;
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
        return "CsgInfraResponse{" +
                "batchSize='" + batchSize + '\'' +
                ", completionTime='" + completionTime + '\'' +
                ", indexUri='" + indexUri + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
