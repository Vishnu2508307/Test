package com.smartsparrow.learner.data;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.PluginReference;

public interface Element extends PluginReference {

    UUID getId();

    CoursewareElementType getElementType();

}
