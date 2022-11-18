package com.smartsparrow.la.lang;

public class EventSummaryCreationException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Unable to create event summary for %s";

    public EventSummaryCreationException(String message) {
        super(String.format(ERROR_MESSAGE, message));
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
