package com.smartsparrow.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class RestExceptionMapper implements ExceptionMapper<Throwable> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(RestExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof ErrorResponseType) {
            ErrorResponseType errorResponseType = (ErrorResponseType) exception;
            return Response.status(errorResponseType.getResponseStatusCode())
                    .entity(new RestError(errorResponseType))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.error("Internal exception: " + exception.getMessage(), exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(new RestError(new ErrorResponseType() {
                    @Override
                    public int getResponseStatusCode() {
                        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
                    }

                    @Override
                    public String getType() {
                        return Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase();
                    }

                    @Override
                    public String getMessage() {
                        return "An internal server error has occurred";
                    }
                }))
                .build();
    }
}
