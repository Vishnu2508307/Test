package com.smartsparrow.annotation.lang;

import java.util.UUID;

import com.smartsparrow.exception.ConflictFault;

public class AnnotationAlreadyExistsFault extends ConflictFault {

    public AnnotationAlreadyExistsFault(UUID annotationId) {
        super(String.format("Annotation id %s already exists", annotationId));
    }

}

