package com.smartsparrow.courseware.data.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.ActivitySummary;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementType;


public class CoursewareElementNode  {

    private UUID elementId;
    private CoursewareElementType type;
    private UUID topParentId;
    private UUID parentId;
    private List<CoursewareElementNode> children;
    private Boolean hasChildren;
    private List<ConfigurationField> configFields;

    public CoursewareElementNode() {
        this.children = new ArrayList<>();
    }

    public CoursewareElementNode(UUID elementId, CoursewareElementType type, UUID topParentId, UUID parentId) {
        this.elementId = elementId;
        this.type = type;
        this.topParentId = topParentId;
        this.parentId = parentId;
        this.children = new ArrayList<>();
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementNode setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getType() {
        return type;
    }

    public CoursewareElementNode setType(CoursewareElementType type) {
        this.type = type;
        return this;
    }

    public UUID getTopParentId() {
        return topParentId;
    }

    public CoursewareElementNode setTopParentId(UUID topParentId) {
        this.topParentId = topParentId;
        return this;
    }


    public UUID getParentId() {
        return parentId;
    }

    public CoursewareElementNode setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public List<CoursewareElementNode> getChildren() {
        return children;
    }

    public CoursewareElementNode addChild(final CoursewareElementNode child) {
        children.add(child);
        return this;
    }

    public List<ConfigurationField> getConfigFields() {
        return configFields;
    }

    public CoursewareElementNode setConfigFields(List<ConfigurationField> configFields) {
        this.configFields = configFields;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementNode that = (CoursewareElementNode) o;
        return Objects.equals(elementId, that.elementId) &&
                type == that.type &&
                Objects.equals(topParentId, that.topParentId) &&
                Objects.equals(parentId, that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, type, topParentId, parentId);
    }

    @Override
    public String toString() {
        return "CoursewareElementNode{" +
                "elementId=" + elementId +
                ", elementType=" + type +
                ", topParentId=" + topParentId +
                ", parentId=" + parentId +
                ", hasChildren=" + hasChildren +
                '}';
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
}
