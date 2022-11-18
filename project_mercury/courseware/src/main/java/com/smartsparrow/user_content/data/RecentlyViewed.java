package com.smartsparrow.user_content.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Recently Viewed Course are tracked against account id
 */
public class RecentlyViewed implements Serializable {

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

    //course last viewed at datetime stamp
    private UUID lastViewedAt;

    public UUID getId() {
        return id;
    }

    public RecentlyViewed setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public RecentlyViewed setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public RecentlyViewed setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public RecentlyViewed setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public RecentlyViewed setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getLastViewedAt() {
        return lastViewedAt;
    }

    public RecentlyViewed setLastViewedAt(final UUID lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
        return this;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public RecentlyViewed setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public RecentlyViewed setDocumentId(final UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public RecentlyViewed setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentlyViewed that = (RecentlyViewed) o;
        return Objects.equals(id, that.id) && Objects.equals(accountId,
                                                             that.accountId) && Objects.equals(
                rootElementId,
                that.rootElementId) && Objects.equals(workspaceId, that.workspaceId) && Objects.equals(
                projectId,
                that.projectId) && Objects.equals(activityId, that.activityId) && Objects.equals(
                documentId,
                that.documentId) && resourceType == that.resourceType && Objects.equals(lastViewedAt,
                                                                                        that.lastViewedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                            accountId,
                            rootElementId,
                            workspaceId,
                            projectId,
                            activityId,
                            documentId,
                            resourceType,
                            lastViewedAt);
    }

    @Override
    public String toString() {
        return "RecentlyViewed{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", rootElementId=" + rootElementId +
                ", workspaceId=" + workspaceId +
                ", projectId=" + projectId +
                ", activityId=" + activityId +
                ", documentId=" + documentId +
                ", resourceType=" + resourceType +
                ", lastViewedAt=" + lastViewedAt +
                '}';
    }
}
