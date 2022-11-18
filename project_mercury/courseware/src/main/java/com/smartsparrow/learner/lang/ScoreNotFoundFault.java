package com.smartsparrow.learner.lang;

import com.smartsparrow.exception.Fault;

public class ScoreNotFoundFault extends Fault {

    public ScoreNotFoundFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 404;
    }

    @Override
    public String getType() {
        return "NOT_FOUND";
    }
}
