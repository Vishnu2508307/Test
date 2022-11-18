package com.smartsparrow.publication.job.data;

import com.smartsparrow.publication.job.enums.NotificationStatus;
import com.smartsparrow.publication.job.enums.NotificationType;

import java.util.Objects;
import java.util.UUID;

public class Notification {

    private UUID id;
    private NotificationType notificationType;
    private NotificationStatus notificationStatus;
    private String message;

    public UUID getId() {
        return id;
    }

    public Notification setId(UUID id) {
        this.id = id;
        return this;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public Notification setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public Notification setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notification setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id) && notificationType == that.notificationType && notificationStatus == that.notificationStatus && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, notificationType, notificationStatus, message);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", notificationType=" + notificationType +
                ", notificationStatus=" + notificationStatus +
                ", message=" + message +
                '}';
    }
}