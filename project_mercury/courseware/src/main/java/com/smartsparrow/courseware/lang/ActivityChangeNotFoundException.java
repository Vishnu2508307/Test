package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ActivityChangeNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "activity change not found for activity %s";

    private UUID activityId;

    public ActivityChangeNotFoundException(UUID activityId) {
        super(String.format(ERROR_MESSAGE, activityId));
        this.activityId = activityId;
    }

    public UUID getActivityId() {
        return activityId;
    }
}
