package com.smartsparrow.rtm.message.event.lang;

public class EventPublisherNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "EventPublisher not found for type `%s`. Have you forgot to bind it?";

    public EventPublisherNotFoundException(String type) {
        super(String.format(ERROR_MESSAGE, type));
    }
}
