package com.smartsparrow.la.data;

import java.util.Objects;
import java.util.UUID;

public class EventFailure {
    private UUID eventId;
    private UUID failId;
    private String exceptionMessage;

    public UUID getEventId() {
        return eventId;
    }

    public EventFailure setEventId(UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    public UUID getFailId() {
        return failId;
    }

    public EventFailure setFailId(UUID failId) {
        this.failId = failId;
        return this;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public EventFailure setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventFailure that = (EventFailure) o;
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(failId, that.failId) &&
                Objects.equals(exceptionMessage, that.exceptionMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, failId, exceptionMessage);
    }

    @Override
    public String toString() {
        return "EventFailure{" +
                "eventId=" + eventId +
                ", failId=" + failId +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                '}';
    }
}
