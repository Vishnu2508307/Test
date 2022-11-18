package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ComponentParentNotFound extends CoursewareException {
    private static final String ERROR_MESSAGE = "no parent element for component with id %s";

    private UUID componentId;

    public ComponentParentNotFound(UUID componentId) {
        super(String.format(ERROR_MESSAGE, componentId));
        this.componentId = componentId;
    }

    public UUID getComponentId() {
        return componentId;
    }
}
