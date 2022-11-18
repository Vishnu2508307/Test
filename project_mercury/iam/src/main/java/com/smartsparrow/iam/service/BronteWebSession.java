package com.smartsparrow.iam.service;

import java.util.Objects;

public class BronteWebSession  extends AbstractWebSession<BronteWebToken> {

    private BronteWebToken bronteWebToken;

    public BronteWebSession(final Account account) { super(account); }

    @Override
    public AuthenticationType getAuthenticationType() { return AuthenticationType.BRONTE; }

    @Override
    public BronteWebToken getWebToken() { return bronteWebToken; }

    public BronteWebSession setBronteWebToken(final BronteWebToken bronteWebToken) {
        this.bronteWebToken = bronteWebToken;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BronteWebSession that = (BronteWebSession) o;
        return Objects.equals(bronteWebToken, that.bronteWebToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bronteWebToken);
    }

    @Override
    public String toString() {
        return "BronteWebSession{" +
                "bronteWebToken=" + bronteWebToken +
                '}';
    }
}
