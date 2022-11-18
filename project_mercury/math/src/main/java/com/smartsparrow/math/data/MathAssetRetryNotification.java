package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class MathAssetRetryNotification {
    private UUID notificationId;
    private Long delaySec;
    private UUID sourceNotificationId;

    public UUID getNotificationId() {
        return notificationId;
    }

    public MathAssetRetryNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public Long getDelaySec() {
        return delaySec;
    }

    public MathAssetRetryNotification setDelaySec(Long delaySec) {
        this.delaySec = delaySec;
        return this;
    }

    public UUID getSourceNotificationId() {
        return sourceNotificationId;
    }

    public MathAssetRetryNotification setSourceNotificationId(UUID sourceNotificationId) {
        this.sourceNotificationId = sourceNotificationId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetRetryNotification that = (MathAssetRetryNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(delaySec, that.delaySec) &&
                Objects.equals(sourceNotificationId, that.sourceNotificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, delaySec, sourceNotificationId);
    }

    @Override
    public String toString() {
        return "MathAssetRetryNotification{" +
                "notificationId=" + notificationId +
                ", delaySec=" + delaySec +
                ", sourceNotificationId=" + sourceNotificationId +
                '}';
    }
}
