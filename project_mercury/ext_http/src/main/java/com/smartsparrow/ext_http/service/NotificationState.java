package com.smartsparrow.ext_http.service;

import java.util.Objects;
import java.util.UUID;

/**
 * Track state of a Notification Event
 */
public class NotificationState {

    private UUID notificationId;
    private RequestPurpose purpose;
    private UUID referenceId;

    public NotificationState() {
    }

    /**
     * Get the notification identifier
     *
     * @return the notification id
     */
    public UUID getNotificationId() {
        return notificationId;
    }

    /**
     * Set the notification identifier, used by the handlers to track an overall request and retries.
     *
     * @param notificationId the notification id
     * @return this
     */
    public NotificationState setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    /**
     * Get the purpose of the request, drives the business specific callback handlers.
     *
     * @return the purpose
     */
    public RequestPurpose getPurpose() {
        return purpose;
    }

    /**
     * The purpose of the request which drives the business specific callback handlers.
     *
     * @param purpose the purpose of the request
     * @return this
     */
    public NotificationState setPurpose(RequestPurpose purpose) {
        this.purpose = purpose;
        return this;
    }

    /**
     * Get the reference id, used to help clients of this api add tracking info to the notifications.
     *
     * @return the reference id
     */
    public UUID getReferenceId() {
        return referenceId;
    }

    /**
     * Set the reference id, used to help clients of this api add tracking info to the notifications.
     *
     * @param referenceId the reference id
     * @return this
     */
    public NotificationState setReferenceId(final UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationState that = (NotificationState) o;
        return Objects.equals(notificationId, that.notificationId) &&
                purpose == that.purpose &&
                Objects.equals(referenceId, that.referenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, purpose, referenceId);
    }

    @Override
    public String toString() {
        return "NotificationState{" +
                "notificationId=" + notificationId +
                ", purpose=" + purpose +
                ", referenceId=" + referenceId +
                '}';
    }

}
