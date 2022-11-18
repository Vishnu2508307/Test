package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class InteractiveNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "no interactive with id %s";

    private UUID interactiveId;

    public InteractiveNotFoundException(UUID interactiveId) {
        super(String.format(ERROR_MESSAGE, interactiveId));
        this.interactiveId = interactiveId;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }
}
