package com.smartsparrow.iam.service;

import java.util.Objects;

public class BronteCredentials implements Credentials{

    private String email;
    private String password;
    private String bearerToken;

    @Override
    public AuthenticationType getType() { return AuthenticationType.BRONTE; }

    public String getEmail() { return email; }

    public BronteCredentials setEmail(final String email) {
        this.email = email;
        return this;
    }

    public String getPassword() { return password; }

    public BronteCredentials setPassword(final String password) {
        this.password = password;
        return this;
    }

    public String getBearerToken() { return bearerToken; }

    public BronteCredentials setBearerToken(final String bearerToken) {
        this.bearerToken = bearerToken;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BronteCredentials that = (BronteCredentials) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                Objects.equals(bearerToken, that.bearerToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, bearerToken);
    }

    @Override
    public String toString() {
        return "BronteCredentials{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", bearerToken='" + bearerToken + '\'' +
                '}';
    }
}
