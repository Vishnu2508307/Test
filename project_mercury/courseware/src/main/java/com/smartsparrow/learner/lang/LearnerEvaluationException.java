package com.smartsparrow.learner.lang;

import java.util.UUID;

import javax.annotation.Nullable;

public class LearnerEvaluationException extends RuntimeException {

    private final UUID evaluationId;

    public LearnerEvaluationException(String message, Throwable throwable) {
        super(message, throwable);
        this.evaluationId = null;
    }

    public LearnerEvaluationException(String message, Throwable throwable, @Nullable UUID evaluationId) {
        super(message, throwable);
        this.evaluationId = evaluationId;
    }

    public UUID getEvaluationId() {
        return evaluationId;
    }
}
