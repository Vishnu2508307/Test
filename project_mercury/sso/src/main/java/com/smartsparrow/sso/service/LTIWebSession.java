package com.smartsparrow.sso.service;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.AbstractWebSession;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.WebToken;

/**
 * Representation of an lti web session with a generic WebToken
 */
public class LTIWebSession extends AbstractWebSession<WebToken> {

    private WebToken webToken;

    public LTIWebSession(Account account) {
        super(account);
    }

    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.LTI;
    }

    public WebToken getWebToken() {
        return webToken;
    }

    public LTIWebSession setWebToken(WebToken webToken) {
        this.webToken = webToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LTIWebSession that = (LTIWebSession) o;
        return Objects.equals(webToken, that.webToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), webToken);
    }

    @Override
    public String toString() {
        return "LTIWebSession{" +
                "webToken=" + webToken +
                '}';
    }
}
