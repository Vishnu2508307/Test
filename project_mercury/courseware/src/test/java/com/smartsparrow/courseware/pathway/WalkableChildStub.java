package com.smartsparrow.courseware.pathway;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;

public class WalkableChildStub {

    public static WalkableChild buildWalkableChild() {
        return new WalkableChild()
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
    }
}
