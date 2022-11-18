package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ScenarioParentNotFoundException extends CoursewareException {
    private static final String ERROR_MESSAGE = "no parent element for scenario with id %s";

    private UUID scenarioId;

    public ScenarioParentNotFoundException(UUID componentId) {
        super(String.format(ERROR_MESSAGE, componentId));
        this.scenarioId = componentId;
    }

    public UUID getScenarioId() {
        return scenarioId;
    }
}
