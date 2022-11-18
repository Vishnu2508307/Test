package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class BadRequestException extends RuntimeException implements ErrorResponseType {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public String getType() {
        return "BAD_REQUEST";
    }

    @Override
    public int getResponseStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }
}
