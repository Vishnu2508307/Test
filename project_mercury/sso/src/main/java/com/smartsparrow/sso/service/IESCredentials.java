package com.smartsparrow.sso.service;

import java.util.Objects;

import javax.annotation.Nullable;

import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.Credentials;

public class IESCredentials implements Credentials {

    private String pearsonUid;
    private String token;
    private String invalidBearerToken;

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.IES;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public IESCredentials setPearsonUid(String pearsonUid) {
        this.pearsonUid = pearsonUid;
        return this;
    }

    public String getToken() {
        return token;
    }

    public IESCredentials setToken(String token) {
        this.token = token;
        return this;
    }

    @Nullable
    public String getInvalidBearerToken() {
        return invalidBearerToken;
    }

    @Nullable
    public IESCredentials setInvalidBearerToken(String invalidBearerToken) {
        this.invalidBearerToken = invalidBearerToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESCredentials that = (IESCredentials) o;
        return Objects.equals(pearsonUid, that.pearsonUid) &&
                Objects.equals(token, that.token) &&
                Objects.equals(invalidBearerToken, that.invalidBearerToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pearsonUid, token, invalidBearerToken);
    }

    @Override
    public String toString() {
        return "IESCredentials{" +
                "pearsonUid='" + pearsonUid + '\'' +
                ", token='" + token + '\'' +
                ", invalidBearerToken='" + invalidBearerToken + '\'' +
                '}';
    }
}
