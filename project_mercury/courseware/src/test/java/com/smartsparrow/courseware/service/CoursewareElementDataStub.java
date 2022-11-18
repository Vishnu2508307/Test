package com.smartsparrow.courseware.service;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class CoursewareElementDataStub {

    public static CoursewareElement build(@Nonnull CoursewareElementType elementType) {
        return new CoursewareElement()
                .setElementId(UUID.randomUUID())
                .setElementType(elementType);
    }
}
