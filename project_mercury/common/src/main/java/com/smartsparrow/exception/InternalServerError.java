package com.smartsparrow.exception;

import javax.ws.rs.core.Response;

public class InternalServerError extends RuntimeException implements ErrorResponseType {

    public InternalServerError() {
        super("An internal server error has occurred");
    }

    @Override
    public int getResponseStatusCode() {
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    @Override
    public String getType() {
        return "INTERNAL_SERVER_ERROR";
    }
}
