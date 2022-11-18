package com.smartsparrow.iam.service;

import java.util.Map;

import javax.annotation.Nullable;

import com.smartsparrow.config.ConfigurableFeatureValues;

/**
 * Provide the user's authentication information, such as their account.
 *
 * Should be injected by way of a Guice Provider:
 * <pre>
 *   Provider<AuthenticationContext> authenticationContextProvider;
 * </pre>
 *
 */
public interface AuthenticationContext extends WebSession {

    /**
     * Get the account.
     *
     * @return the authenticated account
     */
    Account getAccount();

    /**
     * Get the client id associated with this account.
     *
     * @return the client id or <code>null</code> if the context was not set via RTM
     */
    String getClientId();

    /**
     * Get the related Web Session Token generated during authentication.
     *
     * @return the related WebSessionToken generated during authentication.
     */
    @Deprecated
    WebSessionToken getWebSessionToken();

    /**
     * Get the authentication type for this account
     *
     * @return the related authentication type
     */
    AuthenticationType getAuthenticationType();

    /**
     * Get the pearsonUid for the authenticated account
     *
     * @return the related pearsonUid or null when the authenticationType is not of {@link AuthenticationType#IES}
     */
    @Nullable
    String getPearsonUid();

    /**
     * Get the pearsonToken for the authenticated account
     *
     * @return the related pearsonToken or null when the authenticationType is not of {@link AuthenticationType#IES}
     */
    String getPearsonToken();


    /**
     * Finds a particular shadow attribute for a configurable feature
     *
     * @param configurableFeatureValues the feature to find the shadow attribute for
     * @return the found shadow attribute name or null when this is not defined
     */
    AccountShadowAttributeName getAccountShadowAttribute(final ConfigurableFeatureValues configurableFeatureValues);


}
