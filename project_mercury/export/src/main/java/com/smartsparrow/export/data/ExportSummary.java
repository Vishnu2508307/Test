package com.smartsparrow.export.data;

import com.smartsparrow.courseware.data.CoursewareElementType;

import java.util.Objects;
import java.util.UUID;

public class ExportSummary {

    private static final long serialVersionUID = -6343232562622839398L;

    private UUID id;
    private UUID elementId;
    private UUID accountId;
    private UUID projectId;
    private UUID workspaceId;
    private UUID completedAt;
    private CoursewareElementType elementType;
    private ExportStatus status;
    private String ambrosiaUrl;
    private UUID rootElementId;
    private ExportType exportType;
    private String metadata;

    public UUID getId() {
        return id;
    }

    public ExportSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ExportSummary setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public ExportSummary setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public ExportSummary setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ExportSummary setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public UUID getCompletedAt() {
        return completedAt;
    }

    public ExportSummary setCompletedAt(UUID completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ExportSummary setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public ExportSummary setStatus(ExportStatus status) {
        this.status = status;
        return this;
    }

    public String getAmbrosiaUrl() {
        return ambrosiaUrl;
    }

    public ExportSummary setAmbrosiaUrl(String ambrosiaUrl) {
        this.ambrosiaUrl = ambrosiaUrl;
        return this;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public ExportSummary setRootElementId(UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public ExportSummary setExportType(final ExportType exportType) {
        this.exportType = exportType;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public ExportSummary setMetadata(final String metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportSummary that = (ExportSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(completedAt, that.completedAt) &&
                elementType == that.elementType &&
                status == that.status &&
                Objects.equals(ambrosiaUrl, that.ambrosiaUrl) &&
                Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(exportType, that.exportType) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elementId, accountId, projectId, workspaceId, completedAt, elementType, status, ambrosiaUrl, rootElementId, exportType, metadata);
    }

    @Override
    public String toString() {
        return "ExportSummary{" +
                "id=" + id +
                ", elementId=" + elementId +
                ", accountId=" + accountId +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", completedAt=" + completedAt +
                ", elementType=" + elementType +
                ", status=" + status +
                ", ambrosiaUrl=" + ambrosiaUrl +
                ", rootElementId=" + rootElementId +
                ", exportType=" + exportType +
                ", metadata=" + metadata +
                '}';
    }
}
