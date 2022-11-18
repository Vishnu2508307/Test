package com.smartsparrow.courseware.data;

import com.smartsparrow.courseware.eventmessage.CoursewareAction;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

public class ChangeLogByElement implements CoursewareChangeLog, Serializable {

    private static final long serialVersionUID = 1822601226255938287L;

    private UUID id;
    private UUID elementId;
    private UUID onElementId;
    private UUID onParentWalkableId;
    private UUID accountId;
    private CoursewareElementType onElementType;
    private CoursewareElementType onParentWalkableType;
    private CoursewareAction coursewareAction;
    private String onElementTitle;
    private String onParentWalkableTitle;

    @Override
    public UUID getId() {
        return id;
    }

    public ChangeLogByElement setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ChangeLogByElement setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public UUID getOnElementId() {
        return onElementId;
    }

    public ChangeLogByElement setOnElementId(UUID onElementId) {
        this.onElementId = onElementId;
        return this;
    }

    @Override
    public UUID getOnParentWalkableId() {
        return onParentWalkableId;
    }

    public ChangeLogByElement setOnParentWalkableId(UUID onParentWalkableId) {
        this.onParentWalkableId = onParentWalkableId;
        return this;
    }

    @Override
    public UUID getAccountId() {
        return accountId;
    }

    @Nullable
    @Override
    public String getOnElementTitle() {
        return onElementTitle;
    }

    @Nullable
    @Override
    public String getOnParentWalkableTitle() {
        return onParentWalkableTitle;
    }

    public ChangeLogByElement setOnParentWalkableTitle(String onParentWalkableTitle) {
        this.onParentWalkableTitle = onParentWalkableTitle;
        return this;
    }

    public ChangeLogByElement setOnElementTitle(String onElementTitle) {
        this.onElementTitle = onElementTitle;
        return this;
    }

    public ChangeLogByElement setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public CoursewareElementType getOnElementType() {
        return onElementType;
    }

    public ChangeLogByElement setOnElementType(CoursewareElementType onElementType) {
        this.onElementType = onElementType;
        return this;
    }

    @Override
    public CoursewareElementType getOnParentWalkableType() {
        return onParentWalkableType;
    }

    public ChangeLogByElement setOnParentWalkableType(CoursewareElementType onParentWalkableType) {
        this.onParentWalkableType = onParentWalkableType;
        return this;
    }

    @Override
    public CoursewareAction getCoursewareAction() {
        return coursewareAction;
    }

    public ChangeLogByElement setCoursewareAction(CoursewareAction coursewareAction) {
        this.coursewareAction = coursewareAction;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeLogByElement that = (ChangeLogByElement) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(onElementId, that.onElementId) &&
                Objects.equals(onParentWalkableId, that.onParentWalkableId) &&
                Objects.equals(accountId, that.accountId) &&
                onElementType == that.onElementType &&
                onParentWalkableType == that.onParentWalkableType &&
                coursewareAction == that.coursewareAction &&
                Objects.equals(onElementTitle, that.onElementTitle) &&
                Objects.equals(onParentWalkableTitle, that.onParentWalkableTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elementId, onElementId, onParentWalkableId, accountId, onElementType, onParentWalkableType,
                coursewareAction, onElementTitle, onParentWalkableTitle);
    }

    @Override
    public String toString() {
        return "ChangeLogByElement{" +
                "id=" + id +
                ", elementId=" + elementId +
                ", onElementId=" + onElementId +
                ", onParentWalkableId=" + onParentWalkableId +
                ", accountId=" + accountId +
                ", onElementType=" + onElementType +
                ", onParentWalkableType=" + onParentWalkableType +
                ", coursewareAction=" + coursewareAction +
                ", onElementTitle='" + onElementTitle + '\'' +
                ", onParentWalkableTitle='" + onParentWalkableTitle + '\'' +
                '}';
    }
}
