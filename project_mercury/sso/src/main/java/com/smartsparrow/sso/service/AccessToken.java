package com.smartsparrow.sso.service;

import java.util.Objects;
import java.util.UUID;

/**
 * OIDC tokens exchanged during the Token Request to the IdP.
 */
public class AccessToken {

    private String webSessionToken;
    private UUID id;
    private String state;
    private UUID relyingPartyId;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    public AccessToken() {
    }

    public String getWebSessionToken() {
        return webSessionToken;
    }

    public AccessToken setWebSessionToken(String webSessionToken) {
        this.webSessionToken = webSessionToken;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public AccessToken setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getState() {
        return state;
    }

    public AccessToken setState(String state) {
        this.state = state;
        return this;
    }

    public UUID getRelyingPartyId() {
        return relyingPartyId;
    }

    public AccessToken setRelyingPartyId(UUID relyingPartyId) {
        this.relyingPartyId = relyingPartyId;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AccessToken setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public AccessToken setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public AccessToken setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccessToken that = (AccessToken) o;
        return Objects.equals(webSessionToken, that.webSessionToken) && Objects.equals(id, that.id) && Objects.equals(
                state, that.state) && Objects.equals(relyingPartyId, that.relyingPartyId) && Objects.equals(accessToken,
                                                                                                            that.accessToken)
                && Objects.equals(tokenType, that.tokenType) && Objects.equals(expiresIn, that.expiresIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webSessionToken, id, state, relyingPartyId, accessToken, tokenType, expiresIn);
    }

    @Override
    public String toString() {
        return "AccessToken{" + "webSessionToken='" + webSessionToken + '\'' + ", id=" + id + ", state='" + state + '\''
                + ", relyingPartyId=" + relyingPartyId + ", accessToken='" + accessToken + '\'' + ", tokenType='"
                + tokenType + '\'' + ", expiresIn=" + expiresIn + '}';
    }
}
