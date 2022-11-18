package com.smartsparrow.iam.lang;

import org.apache.http.HttpStatus;

import com.smartsparrow.exception.Fault;

public class AeroAccessNotFoundFault extends Fault {

    private static final long serialVersionUID = 3641853642178611725L;

    public AeroAccessNotFoundFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return HttpStatus.SC_FORBIDDEN;
    }

    @Override
    public String getType() {
        return "FORBIDDEN";
    }
}
