package com.smartsparrow.rtm.message.recv.user_content;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.user_content.data.ResourceType;

public class RecentlyViewedMessage extends ReceivedMessage {

    // the account identifier
    public UUID accountId;

    //the root element Id
    public UUID rootElementId;

    //the workplace identifier
    public UUID workspaceId;

    //the project identifier
    public UUID projectId;

    // the activity identifier
    public UUID activityId;

    //the document identifier
    public UUID documentId;

    //favorite resource type
    public ResourceType resourceType;

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentlyViewedMessage that = (RecentlyViewedMessage) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(rootElementId,
                                                                           that.rootElementId) && Objects.equals(
                workspaceId,
                that.workspaceId) && Objects.equals(projectId, that.projectId) && Objects.equals(
                activityId,
                that.activityId) && Objects.equals(documentId,
                                                   that.documentId) && resourceType == that.resourceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, rootElementId, workspaceId, projectId, activityId, documentId, resourceType);
    }

    @Override
    public String toString() {
        return "FavoriteMessage{" +
                "accountId=" + accountId +
                ", rootElementId=" + rootElementId +
                ", workspaceId=" + workspaceId +
                ", projectId=" + projectId +
                ", activityId=" + activityId +
                ", documentId=" + documentId +
                ", resourceType=" + resourceType +
                '}';
    }
}
