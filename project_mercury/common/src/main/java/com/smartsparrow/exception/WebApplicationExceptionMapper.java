package com.smartsparrow.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        return Response.status(exception.getResponse().getStatus())
                .entity(new RestError(new ErrorResponseType() {
                    @Override
                    public int getResponseStatusCode() {
                        return exception.getResponse().getStatus();
                    }

                    @Override
                    public String getType() {
                        return exception.getResponse().getStatusInfo().toEnum().name();
                    }

                    @Override
                    public String getMessage() {
                        return exception.getMessage();
                    }
                }))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
