package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ParentPathwayNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "parent pathway not found for interactive %s";

    private UUID interactiveId;

    public ParentPathwayNotFoundException(UUID interactiveId) {
        super(String.format(ERROR_MESSAGE, interactiveId));
        this.interactiveId = interactiveId;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }

}
