package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ParentInteractiveNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "parent interactive not found for feedback %s";

    private UUID feedbackId;

    public ParentInteractiveNotFoundException(UUID feedbackId) {
        super(String.format(ERROR_MESSAGE, feedbackId));
        this.feedbackId = feedbackId;
    }

    public UUID getFeedbackId() {
        return feedbackId;
    }
}

