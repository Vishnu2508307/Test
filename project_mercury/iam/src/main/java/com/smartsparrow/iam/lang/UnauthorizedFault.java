package com.smartsparrow.iam.lang;

import org.apache.http.HttpStatus;

import com.smartsparrow.exception.Fault;

public class UnauthorizedFault extends Fault {

    public UnauthorizedFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return HttpStatus.SC_UNAUTHORIZED;
    }

    @Override
    public String getType() {
        return "NOT_AUTHORIZED";
    }
}
