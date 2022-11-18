package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.workspace.ProjectMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectIngestionRequestMessage extends ReceivedMessage implements ProjectMessage {

    private UUID projectId;
    private UUID workspaceId;
    private String configFields;
    private String url;
    private String ingestStats;
    private String courseName;
    private UUID rootElementId;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public ProjectIngestionRequestMessage setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ProjectIngestionRequestMessage setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getConfigFields() {
        return configFields;
    }

    public ProjectIngestionRequestMessage setConfigFields(final String configFields) {
        this.configFields = configFields;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ProjectIngestionRequestMessage setUrl(final String url) {
        this.url = url;
        return this;
    }

    public String getIngestStats() {
        return ingestStats;
    }

    public ProjectIngestionRequestMessage setIngestStats(final String ingestStats) {
        this.ingestStats = ingestStats;
        return this;
    }

    public String getCourseName() {
        return courseName;
    }

    public ProjectIngestionRequestMessage setCourseName(final String courseName) {
        this.courseName = courseName;
        return this;
    }

    @Nullable
    public UUID getRootElementId() {
        return rootElementId;
    }

    public ProjectIngestionRequestMessage setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectIngestionRequestMessage that = (ProjectIngestionRequestMessage) o;
        return projectId.equals(that.projectId) &&
                workspaceId.equals(that.workspaceId) &&
                configFields.equals(that.configFields) &&
                url.equals(that.url) &&
                ingestStats.equals(that.ingestStats) &&
                courseName.equals(that.courseName) &&
                rootElementId.equals(that.rootElementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, workspaceId, configFields, url, ingestStats, courseName);
    }

    @Override
    public String toString() {
        return "ProjectIngestionRequestMessage{" +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", configFields='" + configFields + '\'' +
                ", ambrosiaUrl='" + url + '\'' +
                ", ingestStats='" + ingestStats + '\'' +
                ", courseName='" + courseName + '\'' +
                ", rootElementId=" + rootElementId +
                '}';
    }
}
