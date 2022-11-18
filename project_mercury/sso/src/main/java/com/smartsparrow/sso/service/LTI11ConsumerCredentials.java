package com.smartsparrow.sso.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.iam.service.AuthenticationType;

/**
 * LTI version 1.1 consumer credentials representation
 */
public class LTI11ConsumerCredentials implements LTIConsumerCredentials {

    private LTIMessage ltiMessage;
    private String url;
    private String key;
    private String secret;
    private UUID cohortId;
    private UUID workspaceId;
    private String invalidateBearerToken;
    private Map<String, List<String>> httpHeaders;
    private boolean logDebug;
    private final String piToken;

    public LTI11ConsumerCredentials(final @Nullable String piToken) {
        this.piToken = piToken;
    }

    @Override
    public LTIVersion getLTIVersion() {
        return LTIVersion.VERSION_1_1;
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.LTI;
    }

    public LTIMessage getLtiMessage() {
        return ltiMessage;
    }

    public LTI11ConsumerCredentials setLtiMessage(LTIMessage ltiMessage) {
        this.ltiMessage = ltiMessage;
        return this;
    }

    public String getKey() {
        return key;
    }

    public LTI11ConsumerCredentials setKey(String key) {
        this.key = key;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public LTI11ConsumerCredentials setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public LTI11ConsumerCredentials setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    /**
     * The bearer token to invalidate
     *
     * @return the invalid bearer token or null when no existing token is found
     */
    @Nullable
    public String getInvalidateBearerToken() {
        return invalidateBearerToken;
    }

    public LTI11ConsumerCredentials setInvalidateBearerToken(@Nullable String invalidateBearerToken) {
        this.invalidateBearerToken = invalidateBearerToken;
        return this;
    }

    public Map<String, List<String>> getHttpHeaders() {
        return httpHeaders;
    }

    public LTI11ConsumerCredentials setHttpHeaders(Map<String, List<String>> httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    public boolean isLogDebug() {
        return logDebug;
    }

    public LTI11ConsumerCredentials setLogDebug(boolean logDebug) {
        this.logDebug = logDebug;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LTI11ConsumerCredentials setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getPiToken() {
        return piToken;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public LTI11ConsumerCredentials setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTI11ConsumerCredentials that = (LTI11ConsumerCredentials) o;
        return logDebug == that.logDebug &&
                Objects.equals(ltiMessage, that.ltiMessage) &&
                Objects.equals(url, that.url) &&
                Objects.equals(key, that.key) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(invalidateBearerToken, that.invalidateBearerToken) &&
                Objects.equals(httpHeaders, that.httpHeaders) &&
                Objects.equals(piToken, that.piToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ltiMessage, url, key, secret, cohortId, workspaceId, invalidateBearerToken, httpHeaders,
                logDebug, piToken);
    }

    @Override
    public String toString() {
        return "LTI11ConsumerCredentials{" +
                "ltiMessage=" + ltiMessage +
                ", url='" + url + '\'' +
                ", key='" + key + '\'' +
                ", secret='***" + '\'' +
                ", cohortId=" + cohortId +
                ", workspaceId=" + workspaceId +
                ", invalidateBearerToken='" + invalidateBearerToken + '\'' +
                ", httpHeaders=" + httpHeaders +
                ", logDebug=" + logDebug +
                ", piAuthSession=" + piToken +
                '}';
    }
}
