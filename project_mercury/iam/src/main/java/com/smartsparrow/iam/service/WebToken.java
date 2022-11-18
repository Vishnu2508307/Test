package com.smartsparrow.iam.service;

/**
 * This class holds the basic definition of a webToken which is the authorizing entity in a {@link WebSession}
 */
public interface WebToken {

    /**
     * @return the web token type
     */
    WebTokenType getWebTokenType();

    /**
     * @return the web token expiration time
     */
    long getValidUntilTs();

    /**
     * @return the token
     */
    String getToken();
}
