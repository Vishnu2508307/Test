package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

public class LtiConsumerCredentialDetail {

    private UUID cohortId;
    private String key;
    private String secret;
    private Long createdDate;
    private boolean logDebug;

    public UUID getCohortId() {
        return cohortId;
    }

    public LtiConsumerCredentialDetail setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public String getKey() {
        return key;
    }

    public LtiConsumerCredentialDetail setKey(String key) {
        this.key = key;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public LtiConsumerCredentialDetail setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public LtiConsumerCredentialDetail setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public boolean isLogDebug() {
        return logDebug;
    }

    public LtiConsumerCredentialDetail setLogDebug(boolean logDebug) {
        this.logDebug = logDebug;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LtiConsumerCredentialDetail that = (LtiConsumerCredentialDetail) o;
        return logDebug == that.logDebug &&
                Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(key, that.key) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortId, key, secret, createdDate, logDebug);
    }

    @Override
    public String toString() {
        return "LtiConsumerCredentialDetail{" +
                "cohortId=" + cohortId +
                ", key='" + key + '\'' +
                ", secret='" + secret + '\'' +
                ", createdDate=" + createdDate +
                ", logDebug=" + logDebug +
                '}';
    }
}
