package com.smartsparrow.sso.service;

import java.util.Objects;

import javax.annotation.Nullable;

import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.Credentials;

public class MyCloudCredentials implements Credentials {

    private String token;

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.MYCLOUD;
    }

    public String getToken() {
        return token;
    }

    public MyCloudCredentials setToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyCloudCredentials that = (MyCloudCredentials) o;
        return
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "MyCloudCredentials{" +
                ", token='" + token + '\'' +
                '}';
    }
}
