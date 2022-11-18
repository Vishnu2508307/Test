package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ScenarioNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "no scenario with id %s";

    private UUID scenarioId;

    public ScenarioNotFoundException(UUID scenarioId) {
        super(String.format(ERROR_MESSAGE, scenarioId));
        this.scenarioId = scenarioId;
    }

    public UUID getScenarioId() {
        return scenarioId;
    }
}
