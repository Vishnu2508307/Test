package com.smartsparrow.iam.service;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.smartsparrow.config.ConfigurableFeatureValues;

/**
 * An adapter object which provides a context of the authenticated user.
 *
 *
 */
public class MutableAuthenticationContext implements AuthenticationContext {

    private volatile Account account;
    private volatile String clientId;
    private volatile WebSessionToken webSessionToken;
    private volatile AuthenticationType authenticationType;
    private volatile String pearsonUid;
    private volatile String pearsonToken;
    private volatile WebToken webToken;
    private volatile Map<ConfigurableFeatureValues, AccountShadowAttributeName> configuredFeatures;

    public void setAuthenticationType(AuthenticationType type) {
        this.authenticationType = type;
    }

    public AuthenticationType getAuthenticationType() {
        return this.authenticationType;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public void setPearsonUid(String uid) {
        this.pearsonUid = uid;
    }

    public String getPearsonToken() {
        return pearsonToken;
    }

    public MutableAuthenticationContext setConfiguredFeatures(Map<ConfigurableFeatureValues, AccountShadowAttributeName> configuredFeatures) {
        this.configuredFeatures = configuredFeatures;
        return this;
    }

    @Override
    public AccountShadowAttributeName getAccountShadowAttribute(ConfigurableFeatureValues configurableFeatureValues) {
        if (configuredFeatures != null) {
            return configuredFeatures.get(configurableFeatureValues);
        }
        return null;
    }

    public void setPearsonToken(String token) {
        this.pearsonToken = token;
    }

    @Inject
    public MutableAuthenticationContext() {
        account = null;
        authenticationType = AuthenticationType.BRONTE;
    }

    /**
     * Get the account.
     *
     * @return the authenticated account
     */
    @Override
    public Account getAccount() {
        return account;
    }

    /**
     * Set the account in this context
     *
     * @param account the new account
     * @return this
     */
    public AuthenticationContext setAccount(Account account) {
        this.account = account;
        return this;
    }

    /**
     * Get the client id. The client id can be <code>null</code> if the user authenticated with a bearer token and not
     * via rtm
     *
     * @return the initialised client id
     */
    @Override
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the client id in this context
     *
     * @param clientId the client id to set
     * @return
     */
    public MutableAuthenticationContext setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get the related WebSessionToken
     *
     * @return the WebSessionToken
     */
    @Override
    public WebSessionToken getWebSessionToken() {
        return webSessionToken;
    }

    /**
     * Set the WebSessionToken for this context
     *
     * @param webSessionToken the value
     * @return this
     */
    @Deprecated
    public MutableAuthenticationContext setWebSessionToken(WebSessionToken webSessionToken) {
        this.webSessionToken = webSessionToken;
        return this;
    }

    /**
     * Get the related WebToken
     *
     * @return the WebToken
     */
    @Override
    public WebToken getWebToken() {
        return webToken;
    }

    /**
     * Set the WebToken for this context
     *
     * @param webToken the value
     * @return this
     */
    public MutableAuthenticationContext setWebToken(WebToken webToken) {
        this.webToken = webToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MutableAuthenticationContext that = (MutableAuthenticationContext) o;
        return Objects.equals(account, that.account) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(webSessionToken, that.webSessionToken) &&
                Objects.equals(webToken, that.webToken) &&
                Objects.equals(configuredFeatures, that.configuredFeatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, clientId, webSessionToken, webToken, configuredFeatures);
    }

    @Override
    public String toString() {
        return "MutableAuthenticationContext{" +
                "account=" + account +
                ", clientId='" + clientId + '\'' +
                ", webSessionToken=" + webSessionToken +
                ", authenticationType=" + authenticationType +
                ", pearsonUid='" + pearsonUid + '\'' +
                ", pearsonToken='" + pearsonToken + '\'' +
                ", webToken=" + webToken +
                ", configuredFeatures=" + configuredFeatures +
                '}';
    }
}
