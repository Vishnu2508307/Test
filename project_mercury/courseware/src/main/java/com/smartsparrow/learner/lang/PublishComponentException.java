package com.smartsparrow.learner.lang;

import java.util.UUID;

public class PublishComponentException extends PublishCoursewareException {

    private static final String ERROR = "error publishing components for %s. %s";

    private final UUID elementId;

    public PublishComponentException(UUID elementId, String errorMessage) {
        super(String.format(ERROR, elementId, errorMessage));
        this.elementId = elementId;
    }

    public UUID getComponentId() {
        return elementId;
    }
}
