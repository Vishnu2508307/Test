package com.smartsparrow.courseware.lang;

import java.util.UUID;

import com.smartsparrow.exception.ConflictFault;

public class InteractiveAlreadyExistsFault extends ConflictFault {

    public InteractiveAlreadyExistsFault(UUID interactiveId) {
        super(String.format("Interactive id %s already exists", interactiveId));
    }

}
