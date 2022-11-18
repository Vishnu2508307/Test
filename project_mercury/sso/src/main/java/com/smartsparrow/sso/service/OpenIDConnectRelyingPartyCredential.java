package com.smartsparrow.sso.service;

import java.util.Objects;
import java.util.UUID;

public class OpenIDConnectRelyingPartyCredential {

    private UUID relyingPartyId;
    private UUID subscriptionId;

    private String issuerUrl;
    private String clientId;
    private String clientSecret;
    //
    private String authenticationRequestScope;
    private boolean logDebug;
    private boolean enforceVerifiedEmail; //provision only users with verified email

    public OpenIDConnectRelyingPartyCredential() {
    }

    public UUID getRelyingPartyId() {
        return relyingPartyId;
    }

    public OpenIDConnectRelyingPartyCredential setRelyingPartyId(UUID relyingPartyId) {
        this.relyingPartyId = relyingPartyId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public OpenIDConnectRelyingPartyCredential setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public OpenIDConnectRelyingPartyCredential setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public OpenIDConnectRelyingPartyCredential setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OpenIDConnectRelyingPartyCredential setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getAuthenticationRequestScope() {
        return authenticationRequestScope;
    }

    public OpenIDConnectRelyingPartyCredential setAuthenticationRequestScope(String authenticationRequestScope) {
        this.authenticationRequestScope = authenticationRequestScope;
        return this;
    }

    public boolean isLogDebug() {
        return logDebug;
    }

    public OpenIDConnectRelyingPartyCredential setLogDebug(boolean logDebug) {
        this.logDebug = logDebug;
        return this;
    }

    public boolean isEnforceVerifiedEmail() {
        return enforceVerifiedEmail;
    }

    public OpenIDConnectRelyingPartyCredential setEnforceVerifiedEmail(boolean enforceVerifiedEmail) {
        this.enforceVerifiedEmail = enforceVerifiedEmail;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenIDConnectRelyingPartyCredential that = (OpenIDConnectRelyingPartyCredential) o;
        return logDebug == that.logDebug &&
                enforceVerifiedEmail == that.enforceVerifiedEmail &&
                Objects.equals(relyingPartyId, that.relyingPartyId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(issuerUrl, that.issuerUrl) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientSecret, that.clientSecret) &&
                Objects.equals(authenticationRequestScope, that.authenticationRequestScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relyingPartyId, subscriptionId, issuerUrl, clientId, clientSecret, authenticationRequestScope, logDebug, enforceVerifiedEmail);
    }

    @Override
    public String toString() {
        return "OpenIDConnectRelyingPartyCredential{" +
                "relyingPartyId=" + relyingPartyId +
                ", subscriptionId=" + subscriptionId +
                ", issuerUrl='" + issuerUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", authenticationRequestScope='" + authenticationRequestScope + '\'' +
                ", logDebug=" + logDebug +
                ", enforceVerifiedEmail=" + enforceVerifiedEmail +
                '}';
    }
}
