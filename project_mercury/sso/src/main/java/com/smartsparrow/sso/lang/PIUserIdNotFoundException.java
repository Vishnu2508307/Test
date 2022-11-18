package com.smartsparrow.sso.lang;

import java.util.Objects;
import java.util.UUID;

public class PIUserIdNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -4758433096756790990L;

    private UUID launchRequestId;
    private String userId;

    public PIUserIdNotFoundException(String message) {
        super(message);
    }

    public UUID getLaunchRequestId() {
        return launchRequestId;
    }

    public PIUserIdNotFoundException setLaunchRequestId(UUID launchRequestId) {
        this.launchRequestId = launchRequestId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public PIUserIdNotFoundException setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PIUserIdNotFoundException that = (PIUserIdNotFoundException) o;
        return Objects.equals(launchRequestId, that.launchRequestId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(launchRequestId, userId);
    }

    @Override
    public String toString() {
        return "PIUserIdNotFoundException{" +
                "launchRequestId=" + launchRequestId +
                ", userId='" + userId + '\'' +
                '}';
    }
}
