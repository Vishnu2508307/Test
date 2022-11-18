package com.smartsparrow.courseware.payload;

import com.smartsparrow.courseware.data.CoursewareElementType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetCoursewareElementStructureNavigatePayload {

    private UUID elementId;
    private UUID topParentId;
    private UUID parentId;
    private CoursewareElementType type;
    private Boolean hasChildren;
    private List<GetCoursewareElementStructureNavigatePayload> children;
    private List<String> configFields;

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementType getType() {
        return type;
    }

    public List<GetCoursewareElementStructureNavigatePayload> getChildren() {
        return children;
    }

    public UUID getTopParentId() {
        return topParentId;
    }
    public Boolean getHasChildren() {
        return hasChildren;
    }

    public UUID getParentId() {
        return parentId;
    }

    public List<String> getConfigFields() {
        return configFields;
    }

}
