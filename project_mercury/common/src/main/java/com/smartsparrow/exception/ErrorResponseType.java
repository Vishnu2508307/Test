package com.smartsparrow.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Defined as an interface to discourage catch all exception handlers
 */
public interface ErrorResponseType {

    /**
     * See javax.ws.rs.core.Response.Status for correct codes
     * @return a status code based on HTTP status codes.
     */
    int getResponseStatusCode();

    /**
     * lose naming convention for the type is the name of the exception in upper snake case without the exception
     * @return a type string that can be used by API clients to handle the error
     */
    String getType();

    /**
     * Get extended error fields.
     * @return a map of extended error fields
     */
    default Map<String, Object> getExtensions() {
        return Collections.emptyMap();
    }

    /**
     * this will be the message of the exception
     * @return the message of the exception
     */
    String getMessage();
}
