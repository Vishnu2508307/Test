package com.smartsparrow.learner.lang;

import java.util.UUID;

public class PublishPathwayException extends PublishCoursewareException {

    private static final String ERROR = "error publishing pathways for activity %s. %s";

    private final UUID activityId;

    public PublishPathwayException(UUID activityId, String message) {
        super(String.format(ERROR, activityId, message));
        this.activityId = activityId;
    }

    public UUID getActivityId() {
        return activityId;
    }
}
