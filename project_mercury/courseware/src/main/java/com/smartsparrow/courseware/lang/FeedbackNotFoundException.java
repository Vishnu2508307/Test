package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class FeedbackNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "no feedback with id %s";

    private UUID feedbackId;

    public FeedbackNotFoundException(UUID feedbackId) {
        super(String.format(ERROR_MESSAGE, feedbackId));
        this.feedbackId = feedbackId;
    }

    public UUID getFeedbackId() {
        return feedbackId;
    }
}

