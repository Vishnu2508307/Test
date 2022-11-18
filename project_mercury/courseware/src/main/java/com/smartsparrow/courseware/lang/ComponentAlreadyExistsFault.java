package com.smartsparrow.courseware.lang;

import java.util.UUID;

import com.smartsparrow.exception.ConflictFault;

public class ComponentAlreadyExistsFault extends ConflictFault {

    public ComponentAlreadyExistsFault(UUID componentId) {
        super(String.format("Component id %s already exists", componentId));
    }

}
