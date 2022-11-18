package com.smartsparrow.exception;

import javax.ws.rs.core.Response;

public class NotAllowedException extends RuntimeException implements ErrorResponseType {

    public NotAllowedException(String message) { super(message); }

    @Override
    public int getResponseStatusCode() { return Response.Status.FORBIDDEN.getStatusCode(); }

    @Override
    public String getType() { return "FORBIDDEN";  }
}
