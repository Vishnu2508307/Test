package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ComponentNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "no component with id %s";

    private UUID componentId;

    public ComponentNotFoundException(UUID componentId) {
        super(String.format(ERROR_MESSAGE, componentId));
        this.componentId = componentId;
    }

    public UUID getComponentId() {
        return componentId;
    }
}
