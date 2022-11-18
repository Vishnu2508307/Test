package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.eventmessage.CoursewareAction;

public interface CoursewareChangeLog {

    UUID getId();

    UUID getOnElementId();

    CoursewareElementType getOnElementType();

    @Nullable
    UUID getOnParentWalkableId();

    @Nullable
    CoursewareElementType getOnParentWalkableType();

    CoursewareAction getCoursewareAction();

    UUID getAccountId();

    @Nullable
    String getOnElementTitle();

    @Nullable
    String getOnParentWalkableTitle();
}
