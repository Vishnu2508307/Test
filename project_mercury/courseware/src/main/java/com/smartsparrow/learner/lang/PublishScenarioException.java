package com.smartsparrow.learner.lang;

import java.util.UUID;

public class PublishScenarioException extends PublishCoursewareException {

    private static final String ERROR = "error publishing scenario for %s. %s";

    private final UUID elementId;

    public PublishScenarioException(UUID elementId, String message) {
        super(String.format(ERROR, elementId, message));
        this.elementId = elementId;
    }

    public UUID getElementId() {
        return elementId;
    }
}
