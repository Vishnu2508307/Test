package com.smartsparrow.iam.service;

/**
 * Describes the basic representation of {@link Credentials}
 */
public interface Credentials {

    /**
     * @return the authentication type this credentials are for
     */
    AuthenticationType getType();
}
