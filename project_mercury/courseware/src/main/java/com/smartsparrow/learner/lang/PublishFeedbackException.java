package com.smartsparrow.learner.lang;

import java.util.UUID;

public class PublishFeedbackException extends PublishCoursewareException {

    private static final String ERROR = "error publishing feedback for %s. %s";

    private final UUID interactiveId;

    public PublishFeedbackException(UUID interactiveId, String message) {
        super(String.format(ERROR, interactiveId, message));
        this.interactiveId = interactiveId;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }
}
