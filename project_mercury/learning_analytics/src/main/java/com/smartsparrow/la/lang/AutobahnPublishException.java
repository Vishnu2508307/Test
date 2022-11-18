package com.smartsparrow.la.lang;

import java.util.UUID;

public class AutobahnPublishException extends RuntimeException {

    private static final String ERROR_MESSAGE = "unable to publish event %s to Autobahn.";

    private final UUID eventId;

    public AutobahnPublishException(UUID eventId, Throwable cause) {
        super(String.format(ERROR_MESSAGE, eventId, cause.getMessage()));
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}
