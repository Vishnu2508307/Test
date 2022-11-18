package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class NotFoundException extends RuntimeException implements ErrorResponseType {

    private static final long serialVersionUID = 6695033197930267586L;

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return Status.NOT_FOUND.getStatusCode();
    }

    @Override
    public String getType() {
        return "NOT_FOUND";
    }
}
