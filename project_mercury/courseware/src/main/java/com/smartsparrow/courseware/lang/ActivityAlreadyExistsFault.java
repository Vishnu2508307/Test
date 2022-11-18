package com.smartsparrow.courseware.lang;

import java.util.UUID;

import com.smartsparrow.exception.ConflictFault;

public class ActivityAlreadyExistsFault extends ConflictFault {

    public ActivityAlreadyExistsFault(UUID activityId) {
        super(String.format("Activity id %s already exists", activityId));
    }

}
