package com.smartsparrow.sso.service;

import java.util.Objects;
import java.util.UUID;

/**
 * Basic representation of an lti consumer key
 */
public class LTIConsumerKey {

    private UUID id;
    private String key;
    private String secret;
    private UUID subscriptionId;
    private String comment;
    private boolean logDebug;

    public LTIConsumerKey() {
    }

    public LTIConsumerKey(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public UUID getId() {
        return id;
    }

    public LTIConsumerKey setId(final UUID id) {
        this.id = id;
        return this;
    }

    public LTIConsumerKey setKey(String key) {
        this.key = key;
        return this;
    }

    public LTIConsumerKey setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public LTIConsumerKey setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public LTIConsumerKey setComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public boolean isLogDebug() {
        return logDebug;
    }

    public LTIConsumerKey setLogDebug(boolean logDebug) {
        this.logDebug = logDebug;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIConsumerKey that = (LTIConsumerKey) o;
        return logDebug == that.logDebug &&
                Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, secret, subscriptionId, comment, logDebug);
    }

    @Override
    public String toString() {
        return "LTIConsumerKey{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", secret='" + secret + '\'' +
                ", subscriptionId=" + subscriptionId +
                ", comment='" + comment + '\'' +
                ", logDebug=" + logDebug +
                '}';
    }
}
