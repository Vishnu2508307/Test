package com.smartsparrow.courseware.lang;

import java.util.UUID;

public class ActivityThemeNotFoundException extends CoursewareException {
    private static final String ERROR_MESSAGE = "no activityTheme with id %s";

    private UUID activityThemeId;

    public ActivityThemeNotFoundException(UUID activityThemeId) {
        super(String.format(ERROR_MESSAGE, activityThemeId));
        this.activityThemeId = activityThemeId;
    }

    public UUID getActivityThemeId() {
        return activityThemeId;
    }
}
