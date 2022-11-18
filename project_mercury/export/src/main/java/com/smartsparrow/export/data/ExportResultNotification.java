package com.smartsparrow.export.data;


import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class ExportResultNotification implements Notification {

    private static final long serialVersionUID = -6343239562822826498L;

    private UUID notificationId;
    private UUID elementId;
    private UUID accountId;
    private UUID projectId;
    private UUID workspaceId;
    private CoursewareElementType elementType;
    private UUID completedAt;
    private ExportStatus status;
    private UUID exportId;
    private UUID rootElementId;
    private String ambrosiaSnippet;

    @Override
    public UUID getNotificationId() {
        return notificationId;
    }

    public ExportResultNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ExportResultNotification setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public ExportResultNotification setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public ExportResultNotification setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ExportResultNotification setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ExportResultNotification setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getCompletedAt() {
        return completedAt;
    }

    public ExportResultNotification setCompletedAt(UUID completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    @Override
    public ExportStatus getStatus() {
        return status;
    }

    public ExportResultNotification setStatus(ExportStatus status) {
        this.status = status;
        return this;
    }

    public UUID getExportId() {
        return exportId;
    }

    public ExportResultNotification setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public ExportResultNotification setRootElementId(UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public String getAmbrosiaSnippet() {
        return ambrosiaSnippet;
    }

    public ExportResultNotification setAmbrosiaSnippet(String ambrosiaSnippet) {
        this.ambrosiaSnippet = ambrosiaSnippet;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportResultNotification that = (ExportResultNotification) o;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(elementId, that.elementId) && Objects.equals(accountId, that.accountId) && Objects.equals(projectId, that.projectId) && Objects.equals(workspaceId, that.workspaceId) && elementType == that.elementType && Objects.equals(completedAt, that.completedAt) && status == that.status && Objects.equals(exportId, that.exportId) && Objects.equals(rootElementId, that.rootElementId) && Objects.equals(ambrosiaSnippet, that.ambrosiaSnippet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, elementId, accountId, projectId, workspaceId, elementType, completedAt, status, exportId, rootElementId, ambrosiaSnippet);
    }

    @Override
    public String toString() {
        return "ExportResultNotification{" +
                "notificationId=" + notificationId +
                ", elementId=" + elementId +
                ", accountId=" + accountId +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", elementType=" + elementType +
                ", completedAt=" + completedAt +
                ", status=" + status +
                ", exportId=" + exportId +
                ", rootElementId=" + rootElementId +
                ", ambrosiaSnippet='" + ambrosiaSnippet + '\'' +
                '}';
    }
}
