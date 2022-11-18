package com.smartsparrow.learner.lang;

import java.util.UUID;

public class PublishActivityException extends PublishCoursewareException {

    private static final String ERROR = "error publishing activity %s. %s";

    private final UUID activityId;

    public PublishActivityException(UUID activityId, String errorMessage) {
        super(String.format(ERROR, activityId, errorMessage));
        this.activityId = activityId;
    }

    public UUID getActivityId() {
        return activityId;
    }
}
