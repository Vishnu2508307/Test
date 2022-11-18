package com.smartsparrow.ext_http.service;

import java.util.Objects;
import java.util.UUID;

/**
 * This encapsulates a notification which is a response to a retry event.
 */
public class RetryNotification implements Notification {

    private NotificationState state;
    private Long delaySec;
    private UUID sourceNotificationId;

    public RetryNotification() {
    }

    @Override
    public NotificationState getState() {
        return state;
    }

    public RetryNotification setState(final NotificationState state) {
        this.state = state;
        return this;
    }

    public Long getDelaySec() {
        return delaySec;
    }

    public RetryNotification setDelaySec(final Long delaySec) {
        this.delaySec = delaySec;
        return this;
    }

    public UUID getSourceNotificationId() {
        return sourceNotificationId;
    }

    public RetryNotification setSourceNotificationId(final UUID sourceNotificationId) {
        this.sourceNotificationId = sourceNotificationId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryNotification that = (RetryNotification) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(delaySec, that.delaySec) &&
                Objects.equals(sourceNotificationId, that.sourceNotificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, delaySec, sourceNotificationId);
    }

    @Override
    public String toString() {
        return "RetryNotification{" +
                "state=" + state +
                ", delaySec=" + delaySec +
                ", sourceNotificationId=" + sourceNotificationId +
                '}';
    }
}
