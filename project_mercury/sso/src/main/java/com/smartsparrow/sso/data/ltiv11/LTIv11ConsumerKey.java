package com.smartsparrow.sso.data.ltiv11;

import java.util.Objects;
import java.util.UUID;

public class LTIv11ConsumerKey {

    private UUID id;
    private UUID workspaceId;
    private UUID cohortId;
    private String oauthConsumerKey;
    private String oauthConsumerSecret;
    private UUID consumerConfigurationId;
    private boolean logDebug;

    public UUID getId() {
        return id;
    }

    public LTIv11ConsumerKey setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public LTIv11ConsumerKey setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public LTIv11ConsumerKey setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public String getOauthConsumerKey() {
        return oauthConsumerKey;
    }

    public LTIv11ConsumerKey setOauthConsumerKey(String oauthConsumerKey) {
        this.oauthConsumerKey = oauthConsumerKey;
        return this;
    }

    public String getOauthConsumerSecret() {
        return oauthConsumerSecret;
    }

    public LTIv11ConsumerKey setOauthConsumerSecret(String oauthConsumerSecret) {
        this.oauthConsumerSecret = oauthConsumerSecret;
        return this;
    }

    public UUID getConsumerConfigurationId() {
        return consumerConfigurationId;
    }

    public LTIv11ConsumerKey setConsumerConfigurationId(UUID consumerConfigurationId) {
        this.consumerConfigurationId = consumerConfigurationId;
        return this;
    }

    public boolean isLogDebug() {
        return logDebug;
    }

    public LTIv11ConsumerKey setLogDebug(boolean logDebug) {
        this.logDebug = logDebug;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIv11ConsumerKey that = (LTIv11ConsumerKey) o;
        return logDebug == that.logDebug &&
                Objects.equals(id, that.id) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(oauthConsumerKey, that.oauthConsumerKey) &&
                Objects.equals(oauthConsumerSecret, that.oauthConsumerSecret) &&
                Objects.equals(consumerConfigurationId, that.consumerConfigurationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workspaceId, cohortId, oauthConsumerKey, oauthConsumerSecret, consumerConfigurationId, logDebug);
    }

    @Override
    public String toString() {
        return "LTIv11ConsumerKey{" +
                "id=" + id +
                ", workspaceId=" + workspaceId +
                ", cohortId=" + cohortId +
                ", oauthConsumerKey='" + oauthConsumerKey + '\'' +
                ", oauthConsumerSecret='***" + '\'' +
                ", consumerConfigurationId=" + consumerConfigurationId +
                ", logDebug=" + logDebug +
                '}';
    }
}
