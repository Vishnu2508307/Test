package com.smartsparrow.user_content.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Favorite Courses are tracked against account id
 */
public class Favorite implements Serializable {

    private static final long serialVersionUID = 3788517019659966381L;

    private UUID id;

    // the account identifier
    private UUID accountId;

    //the root element Id
    private UUID rootElementId;

    //the workplace identifier
    private UUID workspaceId;

    //the project identifier
    private UUID projectId;

    // the activity identifier
    private UUID activityId;

    //the document identifier
    private UUID documentId;

    //favorite resource type
    private ResourceType resourceType;

    //course made favorite at datetime stamp
    private UUID createdAt;

    public UUID getId() {
        return id;
    }

    public Favorite setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public Favorite setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public Favorite setDocumentId(final UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Favorite setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Favorite setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public Favorite setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public Favorite setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public Favorite setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public Favorite setCreatedAt(final UUID createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favorite favorite = (Favorite) o;
        return Objects.equals(accountId, favorite.accountId) &&
                Objects.equals(rootElementId, favorite.rootElementId) &&
                Objects.equals(workspaceId, favorite.workspaceId) &&
                Objects.equals(projectId, favorite.projectId) &&
                Objects.equals(activityId, favorite.activityId) &&
                Objects.equals(documentId, favorite.documentId) &&
                resourceType == favorite.resourceType &&
                Objects.equals(createdAt, favorite.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId,
                            rootElementId,
                            workspaceId,
                            projectId,
                            activityId,
                            documentId,
                            resourceType,
                            createdAt);
    }
}
