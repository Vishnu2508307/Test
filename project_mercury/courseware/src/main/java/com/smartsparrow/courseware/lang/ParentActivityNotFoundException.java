package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ParentActivityNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "parent activity not found for pathway %s";

    private UUID pathwayId;

    public ParentActivityNotFoundException(UUID pathwayId) {
        super(String.format(ERROR_MESSAGE, pathwayId));
        this.pathwayId = pathwayId;
    }

    public UUID getPathwayId() {
        return pathwayId;
    }
}
