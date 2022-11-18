package com.smartsparrow.user_content.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class UserContentConfig {
    @JsonProperty("cacheNameOrArn")
    private String cacheNameOrArn;

    public String getCacheNameOrArn() {
        return cacheNameOrArn;
    }

    public UserContentConfig setCacheNameOrArn(String cacheNameOrArn) {
        this.cacheNameOrArn = cacheNameOrArn;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserContentConfig that = (UserContentConfig) o;
        return Objects.equal(cacheNameOrArn, that.cacheNameOrArn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cacheNameOrArn);
    }

    @Override
    public String toString() {
        return "UserContentConfig{" + "cacheNameOrArn='" + cacheNameOrArn + '\'' + '}';
    }
}
