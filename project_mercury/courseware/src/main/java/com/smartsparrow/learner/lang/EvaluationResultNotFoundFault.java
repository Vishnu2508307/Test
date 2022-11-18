package com.smartsparrow.learner.lang;

import java.util.UUID;

import com.smartsparrow.exception.Fault;

public class EvaluationResultNotFoundFault extends Fault {

    private final static String ERROR_MESSAGE = "evaluation result not found for attemptId %s";

    private final UUID attemptId;

    public EvaluationResultNotFoundFault(UUID attemptId) {
        super(String.format(ERROR_MESSAGE, attemptId));
        this.attemptId = attemptId;
    }

    public UUID getAttemptId() {
        return attemptId;
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
