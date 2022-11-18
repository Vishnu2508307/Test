package com.smartsparrow.cohort.wiring;

import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LTIConfig {

    @JsonProperty("key")
    @Nullable
    private String key;

    @JsonProperty("secret")
    @Nullable
    private String secret;

    public String getKey() {
        return key;
    }

    public LTIConfig setKey(final String key) {
        this.key = key;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public LTIConfig setSecret(final String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIConfig that = (LTIConfig) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, secret);
    }

    @Override
    public String toString() {
        return "PassportConfig{" +
                "key='" + key + '\'' +
                ",secret='" + secret + '\'' +
                '}';
    }
}
