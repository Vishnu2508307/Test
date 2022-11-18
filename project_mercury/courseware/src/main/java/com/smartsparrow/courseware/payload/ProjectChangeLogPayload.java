package com.smartsparrow.courseware.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;

public class ProjectChangeLogPayload implements CoursewareChangeLogPayload {

    private UUID id;
    private UUID projectId;
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

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectChangeLogPayload setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    @Override
    public String getCreatedAt() {
        return createdAt;
    }

    public ProjectChangeLogPayload setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String getGivenName() {
        return givenName;
    }

    public ProjectChangeLogPayload setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    public ProjectChangeLogPayload setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    @Override
    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public ProjectChangeLogPayload setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
        return this;
    }

    @Override
    public String getAvatarSmall() {
        return avatarSmall;
    }

    public ProjectChangeLogPayload setAvatarSmall(String avatarSmall) {
        this.avatarSmall = avatarSmall;
        return this;
    }

    public ProjectChangeLogPayload setId(UUID id) {
        this.id = id;
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

    public ProjectChangeLogPayload setOnElementId(UUID onElementId) {
        this.onElementId = onElementId;
        return this;
    }

    @Override
    public UUID getOnParentWalkableId() {
        return onParentWalkableId;
    }

    public ProjectChangeLogPayload setOnParentWalkableId(UUID onParentWalkableId) {
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

    public ProjectChangeLogPayload setOnElementTitle(String onElementTitle) {
        this.onElementTitle = onElementTitle;
        return this;
    }

    public ProjectChangeLogPayload setOnParentWalkableTitle(String onParentWalkableTitle) {
        this.onParentWalkableTitle = onParentWalkableTitle;
        return this;
    }

    public ProjectChangeLogPayload setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public CoursewareElementType getOnElementType() {
        return onElementType;
    }

    public ProjectChangeLogPayload setOnElementType(CoursewareElementType onElementType) {
        this.onElementType = onElementType;
        return this;
    }

    @Override
    public CoursewareElementType getOnParentWalkableType() {
        return onParentWalkableType;
    }

    public ProjectChangeLogPayload setOnParentWalkableType(CoursewareElementType onParentWalkableType) {
        this.onParentWalkableType = onParentWalkableType;
        return this;
    }

    @Override
    public CoursewareAction getCoursewareAction() {
        return coursewareAction;
    }

    public ProjectChangeLogPayload setCoursewareAction(CoursewareAction coursewareAction) {
        this.coursewareAction = coursewareAction;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectChangeLogPayload that = (ProjectChangeLogPayload) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(projectId, that.projectId) &&
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
        return Objects.hash(id, projectId, createdAt, givenName, familyName, primaryEmail, avatarSmall, onElementId,
                onParentWalkableId, accountId, onElementType, onParentWalkableType, coursewareAction, onElementTitle, onParentWalkableTitle);
    }

    @Override
    public String toString() {
        return "ProjectChangeLogPayload{" +
                "id=" + id +
                ", projectId=" + projectId +
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
