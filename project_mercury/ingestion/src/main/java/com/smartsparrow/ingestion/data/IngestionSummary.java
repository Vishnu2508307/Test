package com.smartsparrow.ingestion.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

public class IngestionSummary {

    private UUID id;
    private UUID projectId;
    private UUID workspaceId;
    private String courseName;
    private String configFields;
    private UUID creatorId;
    private String ambrosiaUrl;
    private IngestionStatus status;
    private String ingestionStats;
    private UUID rootElementId;
    private UUID activityId;

    public UUID getActivityId() {
        return activityId;
    }

    public IngestionSummary setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public IngestionSummary setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public IngestionSummary setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public IngestionSummary setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getCourseName() {
        return courseName;
    }

    public IngestionSummary setCourseName(final String courseName) {
        this.courseName = courseName;
        return this;
    }

    public String getConfigFields() {
        return configFields;
    }

    public IngestionSummary setConfigFields(final String configFields) {
        this.configFields = configFields;
        return this;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public IngestionSummary setCreatorId(final UUID creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    @Nullable
    public String getAmbrosiaUrl() {
        return ambrosiaUrl;
    }

    public IngestionSummary setAmbrosiaUrl(final String ambrosiaUrl) {
        this.ambrosiaUrl = ambrosiaUrl;
        return this;
    }

    public IngestionStatus getStatus() {
        return status;
    }

    public IngestionSummary setStatus(final IngestionStatus status) {
        this.status = status;
        return this;
    }

    @Nullable
    public String getIngestionStats() {
        return ingestionStats;
    }

    public IngestionSummary setIngestionStats(final String ingestionStats) {
        this.ingestionStats = ingestionStats;
        return this;
    }

    @Nullable
    public UUID getRootElementId() {
        return rootElementId;
    }

    public IngestionSummary setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionSummary that = (IngestionSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(courseName, that.courseName) &&
                Objects.equals(configFields, that.configFields) &&
                Objects.equals(creatorId, that.creatorId) &&
                Objects.equals(ambrosiaUrl, that.ambrosiaUrl) &&
                status == that.status &&
                Objects.equals(ingestionStats, that.ingestionStats) &&
                Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(activityId, that.activityId) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, workspaceId, courseName, configFields, creatorId, ambrosiaUrl, status, ingestionStats, rootElementId, activityId);
    }

    @Override
    public String toString() {
        return "IngestionSummary{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", courseName='" + courseName + '\'' +
                ", configFields='" + configFields + '\'' +
                ", creatorId=" + creatorId +
                ", ambrosiaUrl='" + ambrosiaUrl + '\'' +
                ", status=" + status +
                ", ingestionStats='" + ingestionStats + '\'' +
                ", rootElementId=" + rootElementId +
                ", activityId=" + activityId +
                '}';
    }
}
