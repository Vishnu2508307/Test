package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

/**
 * TODO change this class so that the element and the action fields are always defined when the object is created.
 * TODO declare them final and add a constructor
 */
public class CoursewareElementBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -4588266055578807856L;

    /**
     * oldParentElement is used when an element is moved to a different pathway
     */
    private CoursewareElement oldParentElement;
    /**
     * parentElement is used for Delete and Move messages to define parent activities, can be null for other events
     */
    private CoursewareElement parentElement;
    private CoursewareElement element;
    private CoursewareAction action;
    private UUID accountId;
    private UUID projectId;
    /**
     * assetId and assetUrl are used for ASSET_OPTIMIZED messages to let the front end know the new resized asset info, can be null for other events
     */
    private UUID assetId;
    private String assetUrl;

    /**
     * scenarioLifecycle is used for scenarios reorder messages, can be null for other messages
     */
    private ScenarioLifecycle scenarioLifecycle;

    /**
     * annotationId is used for annotation service messages, can be null for other messages
     */
    private UUID annotationId;

    /**
     * alfrescoSyncType, isAlfrescoAssetUpdated, and isAlfrescoSyncComplete are used for Alfresco assets update messages, can be null and ignored for other messages
     */
    private AlfrescoAssetSyncType alfrescoSyncType;
    private boolean isAlfrescoAssetUpdated;
    private boolean isAlfrescoSyncComplete;

    public CoursewareElement getOldParentElement() {
        return oldParentElement;
    }

    public CoursewareElementBroadcastMessage setOldParentElement(CoursewareElement oldParentElement) {
        this.oldParentElement = oldParentElement;
        return this;
    }

    public CoursewareElement getParentElement() {
        return parentElement;
    }

    public CoursewareElementBroadcastMessage setParentElement(CoursewareElement parentElement) {
        this.parentElement = parentElement;
        return this;
    }

    public CoursewareElement getElement() {
        return element;
    }

    public CoursewareElementBroadcastMessage setElement(CoursewareElement element) {
        this.element = element;
        return this;
    }

    public CoursewareAction getAction() {
        return action;
    }

    public CoursewareElementBroadcastMessage setAction(@Nonnull CoursewareAction action) {
        this.action = action;
        return this;
    }

    @Nullable
    public ScenarioLifecycle getScenarioLifecycle() {
        return scenarioLifecycle;
    }

    public CoursewareElementBroadcastMessage setScenarioLifecycle(ScenarioLifecycle scenarioLifecycle) {
        this.scenarioLifecycle = scenarioLifecycle;
        return this;
    }

    @Nullable
    public UUID getAnnotationId() { return annotationId; }

    public CoursewareElementBroadcastMessage setAnnotationId(final UUID annotationId) {
        this.annotationId = annotationId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public CoursewareElementBroadcastMessage setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public CoursewareElementBroadcastMessage setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    @Nullable
    public UUID getAssetId() { return assetId; }

    public CoursewareElementBroadcastMessage setAssetId(final UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Nullable
    public String getAssetUrl() { return assetUrl; }

    public CoursewareElementBroadcastMessage setAssetUrl(final String assetUrl) {
        this.assetUrl = assetUrl;
        return this;
    }

    @Nullable
    public AlfrescoAssetSyncType getAlfrescoSyncType() { return alfrescoSyncType; }

    public CoursewareElementBroadcastMessage setAlfrescoSyncType(final AlfrescoAssetSyncType alfrescoSyncType) {
        this.alfrescoSyncType = alfrescoSyncType;
        return this;
    }

    public boolean isAlfrescoAssetUpdated() { return isAlfrescoAssetUpdated; }

    public CoursewareElementBroadcastMessage setAlfrescoAssetUpdated(boolean isAlfrescoAssetUpdated) {
        this.isAlfrescoAssetUpdated = isAlfrescoAssetUpdated;
        return this;
    }

    public boolean isAlfrescoSyncComplete() { return isAlfrescoSyncComplete; }

    public CoursewareElementBroadcastMessage setAlfrescoSyncComplete(boolean isAlfrescoSyncComplete) {
        this.isAlfrescoSyncComplete = isAlfrescoSyncComplete;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementBroadcastMessage that = (CoursewareElementBroadcastMessage) o;
        return Objects.equals(oldParentElement, that.oldParentElement) &&
                Objects.equals(parentElement, that.parentElement) &&
                Objects.equals(element, that.element) &&
                action == that.action &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(projectId, that.projectId) &&
                scenarioLifecycle == that.scenarioLifecycle &&
                Objects.equals(annotationId, that.annotationId) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetUrl, that.assetUrl) &&
                alfrescoSyncType == that.alfrescoSyncType &&
                isAlfrescoAssetUpdated == that.isAlfrescoAssetUpdated &&
                isAlfrescoSyncComplete == that.isAlfrescoSyncComplete;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldParentElement, parentElement, element, action, accountId, projectId, scenarioLifecycle,
                            annotationId, assetId, assetUrl, alfrescoSyncType, isAlfrescoAssetUpdated, isAlfrescoSyncComplete);
    }

    @Override
    public String toString() {
        return "CoursewareElementBroadcastMessage{" +
                "oldParentElement=" + oldParentElement +
                ", parentElement=" + parentElement +
                ", element=" + element +
                ", action=" + action +
                ", accountId=" + accountId +
                ", projectId=" + projectId +
                ", scenarioLifecycle=" + scenarioLifecycle +
                ", assetId=" + assetId +
                ", assetUrl=" + assetUrl +
                ", alfrescoSyncType=" + alfrescoSyncType +
                ", isAlfrescoAssetUpdated=" + isAlfrescoAssetUpdated +
                ", isAlfrescoSyncComplete=" + isAlfrescoSyncComplete +
                '}';
    }
}
