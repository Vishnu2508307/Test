package com.smartsparrow.eval.lang;

import java.util.UUID;

public class ScenarioEvaluationException extends RuntimeException {

    private static final String ERROR_MESSAGE = "failed to evaluate scenario with id `%s`";

    private final UUID scenarioId;

    public ScenarioEvaluationException(UUID scenarioId, Throwable cause) {
        super(String.format(ERROR_MESSAGE, scenarioId), cause);
        this.scenarioId = scenarioId;
    }

    public static String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
