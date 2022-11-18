package com.smartsparrow.iam.lang;

import org.apache.http.HttpStatus;

import com.smartsparrow.exception.Fault;

public class AuthenticationNotSupportedFault extends Fault {

    private static final long serialVersionUID = -1761716215013526012L;

    public AuthenticationNotSupportedFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return HttpStatus.SC_METHOD_NOT_ALLOWED;
    }

    @Override
    public String getType() {
        return "METHOD_NOT_ALLOWED";
    }
}
