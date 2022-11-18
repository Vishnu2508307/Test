package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class NotEnrolledException extends RuntimeException implements ErrorResponseType {

    private static final long serialVersionUID = -6797056825353193030L;

    public NotEnrolledException(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return Status.FORBIDDEN.getStatusCode();
    }

    @Override
    public String getType() {
        return "NOT_ENROLLED";
    }
}
