package com.smartsparrow.iam.service;

import java.util.Objects;

public abstract class AbstractWebToken implements WebToken {

    private final String token;

    protected AbstractWebToken(final String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractWebToken that = (AbstractWebToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "AbstractWebToken{" +
                "token='" + token + '\'' +
                '}';
    }
}
