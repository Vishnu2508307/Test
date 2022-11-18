package com.smartsparrow.learner.lang;

import org.apache.http.HttpStatus;

import com.smartsparrow.exception.Fault;

public class AttemptNotFoundFault extends Fault {

    public AttemptNotFoundFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return HttpStatus.SC_NOT_FOUND;
    }

    @Override
    public String getType() {
        return "NOT_FOUND";
    }
}
