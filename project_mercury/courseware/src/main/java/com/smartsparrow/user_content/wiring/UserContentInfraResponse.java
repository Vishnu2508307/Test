package com.smartsparrow.user_content.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class UserContentInfraResponse {
    @JsonProperty("cacheNameOrArn")
    private String cacheNameOrArn;

    public String getCacheNameOrArn() {
        return cacheNameOrArn;
    }

    public UserContentInfraResponse setCacheNameOrArn(String cacheNameOrArn) {
        this.cacheNameOrArn = cacheNameOrArn;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserContentInfraResponse that = (UserContentInfraResponse) o;
        return Objects.equal(cacheNameOrArn, that.cacheNameOrArn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cacheNameOrArn);
    }

    @Override
    public String toString() {
        return "UserContentInfraResponse{" + "cacheNameOrArn='" + cacheNameOrArn + '\'' + '}';
    }
}
