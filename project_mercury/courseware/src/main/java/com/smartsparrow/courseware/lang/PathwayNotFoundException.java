package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class PathwayNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "no pathway with id %s";

    private UUID pathwayId;

    public PathwayNotFoundException(UUID pathwayId) {
        super(String.format(ERROR_MESSAGE, pathwayId));
        this.pathwayId = pathwayId;
    }

    public UUID getPathwayId() {
        return pathwayId;
    }
}
