package com.smartsparrow.ingestion.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.iam.payload.AccountPayload;

public class IngestionSummaryPayload {

    private UUID id;
    private UUID projectId;
    private UUID workspaceId;
    private String courseName;
    private String configFields;
    private AccountPayload creator;
    private String ambrosiaUrl;
    private IngestionStatus status;
    private String ingestionStats;
    private UUID rootElementId;
    private UUID activityId;

    public UUID getActivityId() {
        return activityId;
    }

    public IngestionSummaryPayload setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public IngestionSummaryPayload setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public IngestionSummaryPayload setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public IngestionSummaryPayload setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getCourseName() {
        return courseName;
    }

    public IngestionSummaryPayload setCourseName(String courseName) {
        this.courseName = courseName;
        return this;
    }

    public String getConfigFields() {
        return configFields;
    }

    public IngestionSummaryPayload setConfigFields(String configFields) {
        this.configFields = configFields;
        return this;
    }

    public AccountPayload getCreator() {
        return creator;
    }

    public IngestionSummaryPayload setCreator(AccountPayload creator) {
        this.creator = creator;
        return this;
    }

    public String getAmbrosiaUrl() {
        return ambrosiaUrl;
    }

    public IngestionSummaryPayload setAmbrosiaUrl(String ambrosiaUrl) {
        this.ambrosiaUrl = ambrosiaUrl;
        return this;
    }

    public IngestionStatus getStatus() {
        return status;
    }

    public IngestionSummaryPayload setStatus(final IngestionStatus status) {
        this.status = status;
        return this;
    }

    @Nullable
    public String getIngestionStats() {
        return ingestionStats;
    }

    public IngestionSummaryPayload setIngestionStats(String ingestionStats) {
        this.ingestionStats = ingestionStats;
        return this;
    }

    @Nullable
    public UUID getRootElementId() {
        return rootElementId;
    }

    public IngestionSummaryPayload setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionSummaryPayload that = (IngestionSummaryPayload) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(courseName, that.courseName) &&
                Objects.equals(configFields, that.configFields) &&
                Objects.equals(creator, that.creator) &&
                Objects.equals(ambrosiaUrl, that.ambrosiaUrl) &&
                Objects.equals(status, that.status) &&
                Objects.equals(ingestionStats, that.ingestionStats) &&
                Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, workspaceId, courseName, configFields, creator, ambrosiaUrl, status, ingestionStats, rootElementId, activityId);
    }

    @Override
    public String toString() {
        return "IngestionSummary{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", courseName='" + courseName + '\'' +
                ", configFields='" + configFields + '\'' +
                ", creatorId=" + creator +
                ", ambrosiaUrl='" + ambrosiaUrl + '\'' +
                ", status='" + status + '\'' +
                ", ingestStats='" + ingestionStats + '\'' +
                ", rootElementId='" + rootElementId + '\'' +
                ", activityId='" + activityId + '\'' +
                '}';
    }
}
