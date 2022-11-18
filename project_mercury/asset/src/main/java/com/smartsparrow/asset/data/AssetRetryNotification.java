package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetRetryNotification {
    private UUID notificationId;
    private Long delaySec;
    private UUID sourceNotificationId;

    public UUID getNotificationId() {
        return notificationId;
    }

    public AssetRetryNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public Long getDelaySec() {
        return delaySec;
    }

    public AssetRetryNotification setDelaySec(Long delaySec) {
        this.delaySec = delaySec;
        return this;
    }

    public UUID getSourceNotificationId() {
        return sourceNotificationId;
    }

    public AssetRetryNotification setSourceNotificationId(UUID sourceNotificationId) {
        this.sourceNotificationId = sourceNotificationId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetRetryNotification that = (AssetRetryNotification) o;
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
        return "AssetRetryNotification{" +
                "notificationId=" + notificationId +
                ", delaySec=" + delaySec +
                ", sourceNotificationId=" + sourceNotificationId +
                '}';
    }
}
