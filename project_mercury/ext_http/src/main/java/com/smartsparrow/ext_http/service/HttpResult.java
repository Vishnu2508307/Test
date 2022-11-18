package com.smartsparrow.ext_http.service;

import java.util.Objects;
import java.util.UUID;

public class HttpResult {

    private UUID id;
    private UUID notificationId;
    private UUID sequenceId;
    private HttpEvent event;

    public UUID getId() {
        return id;
    }

    public HttpResult setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public HttpResult setNotificationId(final UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public UUID getSequenceId() {
        return sequenceId;
    }

    public HttpResult setSequenceId(final UUID sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    public HttpEvent getEvent() {
        return event;
    }

    public HttpResult setEvent(final HttpEvent event) {
        this.event = event;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpResult that = (HttpResult) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(sequenceId, that.sequenceId) &&
                Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, notificationId, sequenceId, event);
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "id=" + id +
                ", notificationId=" + notificationId +
                ", sequenceId=" + sequenceId +
                ", event=" + event +
                '}';
    }

}
