package com.smartsparrow.iam.exception;

import com.smartsparrow.exception.ErrorResponseType;
import com.smartsparrow.exception.Fault;

/**
 * Exception to constitute an illegal attempt at accessing a resource
 *
 */
public class PermissionFault extends Fault implements ErrorResponseType {
    private static final long serialVersionUID = 6389292023933773933L;

    // TODO: add a default message to standardize the response.

    public PermissionFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 403;
    }

    @Override
    public String getType() {
        return "FORBIDDEN";
    }
}
