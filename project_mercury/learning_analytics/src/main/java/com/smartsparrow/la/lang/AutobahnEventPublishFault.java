package com.smartsparrow.la.lang;

import java.util.UUID;

import com.smartsparrow.exception.Fault;

public class AutobahnEventPublishFault extends Fault {
    private static final String ERROR_MESSAGE = "unable to publish event %s.";

    private final UUID eventId;

    public AutobahnEventPublishFault(UUID eventId, Throwable cause) {
        super(String.format(ERROR_MESSAGE, eventId, cause.getMessage()));
        this.eventId = eventId;
    }

    @Override
    public int getResponseStatusCode() {
        return 422;
    }

    @Override
    public String getType() {
        return "UNPROCESSABLE_ENTITY";
    }

    public UUID getEventId() {
        return eventId;
    }
}
