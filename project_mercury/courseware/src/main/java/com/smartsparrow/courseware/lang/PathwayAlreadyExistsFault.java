package com.smartsparrow.courseware.lang;

import java.util.UUID;

import com.smartsparrow.exception.ConflictFault;

public class PathwayAlreadyExistsFault extends ConflictFault {

    public PathwayAlreadyExistsFault(UUID pathwayId) {
        super(String.format("Pathway id %s already exists", pathwayId));
    }

}
