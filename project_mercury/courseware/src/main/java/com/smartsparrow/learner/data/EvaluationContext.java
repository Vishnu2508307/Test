package com.smartsparrow.learner.data;

public interface EvaluationContext {

    enum Type {
        LEARNER,
        TEST
    }

    Type getType();
}
