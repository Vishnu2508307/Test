package com.smartsparrow.rtm.message.event.lang;

public class EventPublisherException extends RuntimeException {

    private static final String ERROR = "event publisher error: %s";

    public EventPublisherException(String cause) {
        super(String.format(ERROR, cause));
    }
}
