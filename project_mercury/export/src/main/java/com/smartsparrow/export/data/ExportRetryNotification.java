package com.smartsparrow.export.data;

import java.util.Objects;
import java.util.UUID;

public class ExportRetryNotification implements Notification{
    private UUID notificationId;
    private Long delaySec;
    private UUID sourceNotificationId;

    @Override
    public UUID getNotificationId() {
        return notificationId;
    }

    @Override
    public ExportStatus getStatus() {
        return null;
    }

    public ExportRetryNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public Long getDelaySec() {
        return delaySec;
    }

    public ExportRetryNotification setDelaySec(Long delaySec) {
        this.delaySec = delaySec;
        return this;
    }

    public UUID getSourceNotificationId() {
        return sourceNotificationId;
    }

    public ExportRetryNotification setSourceNotificationId(UUID sourceNotificationId) {
        this.sourceNotificationId = sourceNotificationId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportRetryNotification that = (ExportRetryNotification) o;
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
        return "ExportRetryNotification{" +
                "notificationId=" + notificationId +
                ", delaySec=" + delaySec +
                ", sourceNotificationId=" + sourceNotificationId +
                '}';
    }
}
