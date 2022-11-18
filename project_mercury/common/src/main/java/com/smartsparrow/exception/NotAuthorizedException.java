package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class NotAuthorizedException extends RuntimeException implements ErrorResponseType {

    private static final long serialVersionUID = 1849258604743179843L;
    public static final String DEFAULT_MESSAGE = "Not authorized to access this resource";

    public NotAuthorizedException() {
        this(DEFAULT_MESSAGE);
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    @Override
    public String getType() {
        return "NOT_AUTHORIZED";
    }

    @Override
    public int getResponseStatusCode() {
        return Status.UNAUTHORIZED.getStatusCode();
    }
}
