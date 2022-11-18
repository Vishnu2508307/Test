package com.smartsparrow.iam.wiring;

import javax.inject.Provider;

import com.smartsparrow.iam.service.MutableAuthenticationContext;

/**
 * Provide injection of MutableAuthenticationContext objects.
 */
public class MutableAuthenticationContextProvider implements Provider<MutableAuthenticationContext> {

    @Override
    public MutableAuthenticationContext get() {
        return AuthenticationProvider.get();
    }

    /**
     * Remove any set thread local values.
     */
    public static void cleanup() {
        AuthenticationProvider.cleanup();
    }

    /**
     * Set the thread local authentication context value.
     *
     * @param value the value to set in the thread local.
     */
    public static void set(MutableAuthenticationContext value) {
        AuthenticationProvider.set(value);
    }
}
