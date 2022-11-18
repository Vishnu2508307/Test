package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class ConflictException extends RuntimeException implements ErrorResponseType {

    public ConflictException(String message) {
        super(message);
    }

    @Override
    public String getType() {
        return "CONFLICT";
    }

    @Override
    public int getResponseStatusCode() {
        return Status.CONFLICT.getStatusCode();
    }
}
