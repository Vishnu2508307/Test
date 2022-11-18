package com.smartsparrow.sso.data.ltiv11;

import java.util.Objects;
import java.util.UUID;

public class LTI11LaunchSessionHash {

    public enum Status {
        VALID,
        EXPIRED
    }

    private String hash;
    private UUID launchRequestId;
    private String userId;
    private UUID cohortId;
    private UUID configurationId;
    private String continueTo;
    private Status status;

    public String getHash() {
        return hash;
    }

    public LTI11LaunchSessionHash setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public UUID getLaunchRequestId() {
        return launchRequestId;
    }

    public LTI11LaunchSessionHash setLaunchRequestId(UUID launchRequestId) {
        this.launchRequestId = launchRequestId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public LTI11LaunchSessionHash setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public LTI11LaunchSessionHash setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public LTI11LaunchSessionHash setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
        return this;
    }

    public String getContinueTo() {
        return continueTo;
    }

    public LTI11LaunchSessionHash setContinueTo(String continueTo) {
        this.continueTo = continueTo;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public LTI11LaunchSessionHash setStatus(Status status) {
        this.status = status;
        return this;
    }

    public boolean isExpired() {
        return this.status.equals(Status.EXPIRED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTI11LaunchSessionHash that = (LTI11LaunchSessionHash) o;
        return Objects.equals(hash, that.hash) &&
                Objects.equals(launchRequestId, that.launchRequestId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(configurationId, that.configurationId) &&
                Objects.equals(continueTo, that.continueTo) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, launchRequestId, userId, cohortId, configurationId, continueTo, status);
    }

    @Override
    public String toString() {
        return "LTI11LaunchSessionHash{" +
                "hash='" + hash + '\'' +
                ", launchRequestId=" + launchRequestId +
                ", userId='" + userId + '\'' +
                ", cohortId=" + cohortId +
                ", configurationId=" + configurationId +
                ", continueTo='" + continueTo + '\'' +
                ", status=" + status +
                '}';
    }
}
