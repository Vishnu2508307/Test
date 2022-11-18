package com.smartsparrow.exception;

import javax.ws.rs.core.Response;

public class NotAcceptableException extends RuntimeException implements ErrorResponseType {

    public NotAcceptableException(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return Response.Status.NOT_ACCEPTABLE.getStatusCode();
    }

    @Override
    public String getType() {
        return "NOT_ACCEPTABLE";
    }
}
