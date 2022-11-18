package com.smartsparrow.cohort.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.cohort.data.LtiConsumerCredentialDetail;

public class LtiConsumerCredential {

    private String key;
    private String secret;

    @JsonIgnore
    public static LtiConsumerCredential from(@Nonnull LtiConsumerCredentialDetail ltiConsumerCredentialDetail) {
        checkNotNull(ltiConsumerCredentialDetail);

        return new LtiConsumerCredential()
                .setKey(ltiConsumerCredentialDetail.getKey())
                .setSecret(ltiConsumerCredentialDetail.getSecret());
    }

    public String getKey() {
        return key;
    }

    public LtiConsumerCredential setKey(String key) {
        this.key = key;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public LtiConsumerCredential setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LtiConsumerCredential that = (LtiConsumerCredential) o;
        return Objects.equals(key, that.key) && Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, secret);
    }

    @Override
    public String toString() {
        return "LtiConsumerCredential{" +
                "key='" + key + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }
}
