package com.smartsparrow.export.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.courseware.payload.ScenarioPayload;
import com.smartsparrow.plugin.payload.ExportPluginPayload;
import com.smartsparrow.util.UUIDs;

public class ExportRequestNotification extends ExportRequest implements Notification {

    private static final long serialVersionUID = -6343232562622866398L;

    private UUID notificationId;
    private ActivityPayload activity;
    private ActivityPayload rootElement;
    private PathwayPayload pathway;
    private InteractivePayload interactive;
    private ComponentPayload component;
    private List<ScenarioPayload> scenarios;
    private List<CoursewareAnnotation> annotations;
    private List<AssetPayload> resolvedAssets;
    private ExportPluginPayload pluginPayload;
    private UUID exportElementId;

    public ExportRequestNotification(UUID elementId) {
        this.elementId = elementId;
    }

    public ExportRequestNotification(UUID elementId,
                                     CoursewareElementType elementType,
                                     ExportRequest exportRequest) {

        this.notificationId = UUIDs.timeBased();
        this.elementId = elementId;
        this.elementType = elementType;
        //
        this.accountId = exportRequest.getAccountId();
        this.projectId = exportRequest.getProjectId();
        this.workspaceId = exportRequest.getWorkspaceId();
        this.exportId = exportRequest.getExportId();
        this.status = exportRequest.getStatus();
        this.metadata = exportRequest.getMetadata();

        // for every notification object, the rootElementId for that export and export requested element ID are carried over
        this.rootElementId = exportRequest.getRootElementId();
        this.exportElementId = exportRequest.getElementId();
    }

    public boolean isRootElementExport() {
        return this.rootElementId == this.exportElementId;
    }

    public UUID getExportElementId() {
        return exportElementId;
    }
    
    public ExportPluginPayload getPluginPayload() {
        return pluginPayload;
    }

    public ExportRequestNotification setPluginPayload(final ExportPluginPayload pluginPayload) {
        this.pluginPayload = pluginPayload;
        return this;
    }

    @Override
    public UUID getNotificationId() {
        return notificationId;
    }

    public ExportRequestNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public ExportRequestNotification setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public ExportRequestNotification setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ExportRequestNotification setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ExportRequestNotification setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public ExportStatus getStatus() {
        return status;
    }

    public ExportRequestNotification setStatus(ExportStatus status) {
        this.status = status;
        return this;
    }

    public UUID getExportId() {
        return exportId;
    }

    public ExportRequestNotification setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public ExportRequestNotification setRootElementId(UUID rootElementId) {
        this.rootElementId = rootElementId;
        this.exportElementId = this.elementId;
        return this;
    }

    public ActivityPayload getActivity() {
        return activity;
    }

    public ExportRequestNotification setActivity(final ActivityPayload activity) {
        this.activity = activity;
        return this;
    }

    public ActivityPayload getRootElement() {
        return rootElement;
    }

    public ExportRequestNotification setRootElement(final ActivityPayload rootElement) {
        this.rootElement = rootElement;
        return this;
    }

    public PathwayPayload getPathway() {
        return pathway;
    }

    public ExportRequestNotification setPathway(final PathwayPayload pathway) {
        this.pathway = pathway;
        return this;
    }

    public InteractivePayload getInteractive() {
        return interactive;
    }

    public ExportRequestNotification setInteractive(final InteractivePayload interactive) {
        this.interactive = interactive;
        return this;
    }

    public ComponentPayload getComponent() {
        return component;
    }

    public ExportRequestNotification setComponent(final ComponentPayload component) {
        this.component = component;
        return this;
    }

    public List<ScenarioPayload> getScenarios() {
        return scenarios;
    }

    public ExportRequestNotification setScenarios(final List<ScenarioPayload> scenarios) {
        this.scenarios = scenarios;
        return this;
    }

    public List<CoursewareAnnotation> getAnnotations() {
        return annotations;
    }

    public ExportRequestNotification setAnnotations(final List<CoursewareAnnotation> annotations) {
        this.annotations = annotations;
        return this;
    }

    public List<AssetPayload> getResolvedAssets() {
        return resolvedAssets;
    }

    public ExportRequestNotification setResolvedAssets(final List<AssetPayload> resolvedAssets) {
        this.resolvedAssets = resolvedAssets;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public ExportRequestNotification setMetadata(final String metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportRequestNotification that = (ExportRequestNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(exportId, that.exportId) &&
                elementType == that.elementType &&
                status == that.status &&
                Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(activity, that.activity) &&
                Objects.equals(rootElement, that.rootElement) &&
                Objects.equals(pathway, that.pathway) &&
                Objects.equals(interactive, that.interactive) &&
                Objects.equals(component, that.component) &&
                Objects.equals(scenarios, that.scenarios) &&
                Objects.equals(annotations, that.annotations) &&
                Objects.equals(resolvedAssets, that.resolvedAssets) &&
                Objects.equals(pluginPayload, that.pluginPayload) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId,
                            elementId,
                            accountId,
                            projectId,
                            workspaceId,
                            exportId,
                            elementType,
                            status,
                            rootElementId,
                            activity,
                            rootElement,
                            pathway,
                            interactive,
                            component,
                            scenarios,
                            annotations,
                            resolvedAssets,
                            pluginPayload,
                            metadata);
    }

    @Override
    public String toString() {
        return "ExportRequestNotification{" +
                "notificationId=" + notificationId +
                ", elementId=" + elementId +
                ", accountId=" + accountId +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", exportId=" + exportId +
                ", elementType=" + elementType +
                ", status=" + status +
                ", rootElementId=" + rootElementId +
                ", activity=" + activity +
                ", rootElement=" + rootElement +
                ", pathway=" + pathway +
                ", interactive=" + interactive +
                ", component=" + component +
                ", scenarios=" + scenarios +
                ", annotations=" + annotations +
                ", resolvedAssets=" + resolvedAssets +
                ", pluginPayload=" + pluginPayload +
                ", metadata=" + metadata +
                '}';
    }
}
