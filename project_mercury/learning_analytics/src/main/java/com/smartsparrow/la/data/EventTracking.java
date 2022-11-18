package com.smartsparrow.la.data;

import java.util.Objects;
import java.util.UUID;


public class EventTracking {
    private UUID trackingId;
    private UUID eventId;

    public UUID getTrackingId() {
        return trackingId;
    }

    public EventTracking setTrackingId(UUID trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    public UUID getEventId() {
        return eventId;
    }

    public EventTracking setEventId(UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventTracking that = (EventTracking) o;
        return Objects.equals(trackingId, that.trackingId) &&
                Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingId, eventId);
    }

    @Override
    public String toString() {
        return "EventTracking{" +
                "trackingId=" + trackingId +
                ", eventId=" + eventId +
                '}';
    }
}
