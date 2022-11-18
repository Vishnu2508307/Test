package com.smartsparrow.ext_http.service;

import java.util.Objects;
import java.util.UUID;

public class ExternalHttpRequestLogRecord {

    enum Event {
        // A request has been submitted for processing
        REQUEST_SUBMITTED,
        // A response has been received for this request
        RESULT_RECEIVED,
        // An error has been received for this request
        ERROR,
        // A message has been submitted to the delay queue to be retried
        RETRY_DELAY_SUBMITTED,
        // The retry message has been received back from the delay queue
        RETRY_RECEIVED
    }

    private UUID id;
    private Event event;
    private UUID notificationId;

    public UUID getId() {
        return id;
    }

    public ExternalHttpRequestLogRecord setId(final UUID id) {
        this.id = id;
        return this;
    }

    public Event getEvent() {
        return event;
    }

    public ExternalHttpRequestLogRecord setEvent(final Event event) {
        this.event = event;
        return this;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public ExternalHttpRequestLogRecord setNotificationId(final UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalHttpRequestLogRecord that = (ExternalHttpRequestLogRecord) o;
        return Objects.equals(id, that.id) &&
                event == that.event &&
                Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, event, notificationId);
    }

    @Override
    public String toString() {
        return "ExternalHttpRequestLogRecord{" +
                "id=" + id +
                ", event=" + event +
                ", notificationId=" + notificationId +
                '}';
    }
}
