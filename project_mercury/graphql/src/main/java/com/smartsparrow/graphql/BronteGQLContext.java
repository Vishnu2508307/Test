package com.smartsparrow.graphql;

import java.util.Objects;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;

/**
 * Use this context to pass down to queries and mutations
 */
public class BronteGQLContext {
    private MutableAuthenticationContext mutableAuthenticationContext;
    private AuthenticationContext authenticationContext;

    public MutableAuthenticationContext getMutableAuthenticationContext() {
        return mutableAuthenticationContext;
    }

    public BronteGQLContext setMutableAuthenticationContext(final MutableAuthenticationContext mutableAuthenticationContext) {
        this.mutableAuthenticationContext = mutableAuthenticationContext;
        return this;
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    public BronteGQLContext setAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteGQLContext that = (BronteGQLContext) o;
        return Objects.equals(mutableAuthenticationContext,
                              that.mutableAuthenticationContext) && Objects.equals(authenticationContext,
                                                                                   that.authenticationContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mutableAuthenticationContext, authenticationContext);
    }

    @Override
    public String toString() {
        return "BronteGQLContext{" +
                "mutableAuthenticationContext=" + mutableAuthenticationContext +
                ", authenticationContext=" + authenticationContext +
                '}';
    }
}
