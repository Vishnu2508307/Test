package com.smartsparrow.iam.service;

/**
 * Represents a WebSession for an authenticated account
 *
 * @param <T> the web token type
 */
public interface WebSession<T extends WebToken> {

    /**
     * @return the authentication type used to exchange {@link Credentials} for this web session
     */
    AuthenticationType getAuthenticationType();

    /**
     * @return the web token for this session
     */
    T getWebToken();

    /**
     * @return the Bronte account associated with this session
     */
    Account getAccount();
}
