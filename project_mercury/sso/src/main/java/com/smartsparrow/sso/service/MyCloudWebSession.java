package com.smartsparrow.sso.service;

import java.util.Objects;

import com.smartsparrow.iam.service.AbstractWebSession;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.WebSessionToken;

/**
 * Representation of an my cloud web session with an my cloud web token
 */
public class MyCloudWebSession extends AbstractWebSession<MyCloudWebToken> {

    private MyCloudWebToken myCloudWebToken;

    public MyCloudWebSession(Account account) {
        super(account);
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.MYCLOUD;
    }

    @Override
    public MyCloudWebToken getWebToken() {
        return myCloudWebToken;
    }

    public MyCloudWebSession setMyCloudWebToken(MyCloudWebToken myCloudWebToken) {
        this.myCloudWebToken = myCloudWebToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MyCloudWebSession that = (MyCloudWebSession) o;
        return Objects.equals(myCloudWebToken, that.myCloudWebToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), myCloudWebToken);
    }

    @Override
    public String toString() {
        return "MyCloudWebSession{" +
                "myCloudWebToken=" + myCloudWebToken +
                '}';
    }
}
