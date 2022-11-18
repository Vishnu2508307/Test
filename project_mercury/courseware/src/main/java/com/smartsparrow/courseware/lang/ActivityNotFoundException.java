package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ActivityNotFoundException extends CoursewareException {

    private static final String ERROR_MESSAGE = "no activity with id %s";

    private UUID activityId;

    public ActivityNotFoundException(UUID activityId) {
        super(String.format(ERROR_MESSAGE, activityId));
        this.activityId = activityId;
    }

    public UUID getActivityId() {
        return activityId;
    }


}
