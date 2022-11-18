package com.smartsparrow.sso.service;

import java.util.Objects;

import com.smartsparrow.iam.service.AbstractWebSession;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.WebSessionToken;

/**
 * Representation of an ies web session with an ies web token
 */
public class IESWebSession extends AbstractWebSession<IESWebToken> {

    private IESWebToken iesWebToken;

    public IESWebSession(Account account) {
        super(account);
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.IES;
    }

    @Override
    public IESWebToken getWebToken() {
        return iesWebToken;
    }

    public IESWebSession setIesWebToken(IESWebToken iesWebToken) {
        this.iesWebToken = iesWebToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IESWebSession that = (IESWebSession) o;
        return Objects.equals(iesWebToken, that.iesWebToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), iesWebToken);
    }

    @Override
    public String toString() {
        return "IESWebSession{" +
                "iesWebToken=" + iesWebToken +
                '}';
    }
}
