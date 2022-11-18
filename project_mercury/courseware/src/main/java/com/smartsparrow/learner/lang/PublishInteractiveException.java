package com.smartsparrow.learner.lang;

import java.util.UUID;

public class PublishInteractiveException extends PublishCoursewareException {

    private static final String ERROR = "error publishing interactive %s. %s";

    private final UUID interactiveId;

    public PublishInteractiveException(UUID interactiveId, String message) {
        super(String.format(ERROR, interactiveId, message));
        this.interactiveId = interactiveId;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }
}
