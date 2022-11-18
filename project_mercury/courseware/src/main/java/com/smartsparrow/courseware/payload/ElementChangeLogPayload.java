package com.smartsparrow.courseware.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;

public class ElementChangeLogPayload implements CoursewareChangeLogPayload {

    private UUID id;
    private UUID elementId;
    private String createdAt;
    private String givenName;
    private String familyName;
    private String primaryEmail;
    private String avatarSmall;
    private UUID onElementId;
    private UUID onParentWalkableId;
    private UUID accountId;
    private CoursewareElementType onElementType;
    private CoursewareElementType onParentWalkableType;
    private CoursewareAction coursewareAction;
    private String onElementTitle;
    private String onParentWalkableTitle;

    public UUID getElementId() {
        return elementId;
    }

    public ElementChangeLogPayload setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public String getCreatedAt() {
        return createdAt;
    }

    public ElementChangeLogPayload setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String getGivenName() {
        return givenName;
    }

    public ElementChangeLogPayload setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    public ElementChangeLogPayload setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    @Override
    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public ElementChangeLogPayload setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
        return this;
    }

    public ElementChangeLogPayload setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public String getAvatarSmall() {
        return avatarSmall;
    }

    public ElementChangeLogPayload setAvatarSmall(String avatarSmall) {
        this.avatarSmall = avatarSmall;
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getOnElementId() {
        return onElementId;
    }

    public ElementChangeLogPayload setOnElementId(UUID onElementId) {
        this.onElementId = onElementId;
        return this;
    }

    @Override
    public UUID getOnParentWalkableId() {
        return onParentWalkableId;
    }

    public ElementChangeLogPayload setOnParentWalkableId(UUID onParentWalkableId) {
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

    public ElementChangeLogPayload setOnElementTitle(String onElementTitle) {
        this.onElementTitle = onElementTitle;
        return this;
    }

    public ElementChangeLogPayload setOnParentWalkableTitle(String onParentWalkableTitle) {
        this.onParentWalkableTitle = onParentWalkableTitle;
        return this;
    }

    public ElementChangeLogPayload setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public CoursewareElementType getOnElementType() {
        return onElementType;
    }

    public ElementChangeLogPayload setOnElementType(CoursewareElementType onElementType) {
        this.onElementType = onElementType;
        return this;
    }

    @Override
    public CoursewareElementType getOnParentWalkableType() {
        return onParentWalkableType;
    }

    public ElementChangeLogPayload setOnParentWalkableType(CoursewareElementType onParentWalkableType) {
        this.onParentWalkableType = onParentWalkableType;
        return this;
    }

    @Override
    public CoursewareAction getCoursewareAction() {
        return coursewareAction;
    }

    public ElementChangeLogPayload setCoursewareAction(CoursewareAction coursewareAction) {
        this.coursewareAction = coursewareAction;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementChangeLogPayload that = (ElementChangeLogPayload) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(givenName, that.givenName) &&
                Objects.equals(familyName, that.familyName) &&
                Objects.equals(primaryEmail, that.primaryEmail) &&
                Objects.equals(avatarSmall, that.avatarSmall) &&
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
        return Objects.hash(id, elementId, createdAt, givenName, familyName, primaryEmail, avatarSmall, onElementId,
                onParentWalkableId, accountId, onElementType, onParentWalkableType, coursewareAction, onElementTitle, onParentWalkableTitle);
    }

    @Override
    public String toString() {
        return "ElementChangeLogPayload{" +
                "id=" + id +
                ", elementId=" + elementId +
                ", createdAt='" + createdAt + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", avatarSmall='" + avatarSmall + '\'' +
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
