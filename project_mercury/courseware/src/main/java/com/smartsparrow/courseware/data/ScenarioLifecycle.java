package com.smartsparrow.courseware.data;

import com.smartsparrow.exception.UnsupportedOperationFault;

public enum ScenarioLifecycle {

    //
    // interactive lifecycle values
    //
    INTERACTIVE_PRE_ENTRY,
    INTERACTIVE_ENTRY,
    INTERACTIVE_DURING,
    INTERACTIVE_EXIT,
    INTERACTIVE_EVALUATE,

    //
    // activity lifecycle values
    //
    ACTIVITY_ENTRY,
    ACTIVITY_START,
    ACTIVITY_COMPLETE,
    ACTIVITY_EVALUATE;

    public static CoursewareElementType getCoursewareElementType(ScenarioLifecycle l) {
        if (l == null) {
            return null;
        }
        if (l.name().contains("ACTIVITY")) {
            return CoursewareElementType.ACTIVITY;
        } else if(l.name().contains("INTERACTIVE")) {
            return CoursewareElementType.INTERACTIVE;
        } else {
            throw new UnsupportedOperationException("Unsupported ScenarioLifecycle type:" + l);
        }
    }

    public static ScenarioLifecycle defaultScenarioLifecycle(CoursewareElementType elementType) {
        switch (elementType) {
            case INTERACTIVE:
                return INTERACTIVE_EVALUATE;
            case ACTIVITY:
                return ACTIVITY_EVALUATE;
            default:
                throw new UnsupportedOperationFault(String.format("scenario lifecycle not support for element type %s", elementType));
        }
    }
}
