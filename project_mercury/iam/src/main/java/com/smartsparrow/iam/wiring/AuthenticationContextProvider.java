package com.smartsparrow.iam.wiring;

import javax.inject.Provider;

import com.smartsparrow.iam.service.AuthenticationContext;

/**
 * Provide injection of AuthenticationContext objects.
 */
public class AuthenticationContextProvider implements Provider<AuthenticationContext> {

    @Override
    public AuthenticationContext get() {
        return AuthenticationProvider.get();
    }

}
