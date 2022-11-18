package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.workspace.ProjectMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectIngestionCreateMessage extends ReceivedMessage implements ProjectMessage {

    private UUID projectId;
    private UUID workspaceId;
    private String configFields;
    private String ingestStats;
    private String courseName;
    private String fileName;
    private UUID rootElementId;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public ProjectIngestionCreateMessage setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ProjectIngestionCreateMessage setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getConfigFields() {
        return configFields;
    }

    public ProjectIngestionCreateMessage setConfigFields(final String configFields) {
        this.configFields = configFields;
        return this;
    }

    public String getIngestStats() {
        return ingestStats;
    }

    public ProjectIngestionCreateMessage setIngestStats(final String ingestStats) {
        this.ingestStats = ingestStats;
        return this;
    }

    public String getCourseName() {
        return courseName;
    }

    public ProjectIngestionCreateMessage setCourseName(final String courseName) {
        this.courseName = courseName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ProjectIngestionCreateMessage setFileName(final String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Nullable
    public UUID getRootElementId() {
        return rootElementId;
    }

    public ProjectIngestionCreateMessage setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectIngestionCreateMessage that = (ProjectIngestionCreateMessage) o;
        return projectId.equals(that.projectId) &&
                workspaceId.equals(that.workspaceId) &&
                configFields.equals(that.configFields) &&
                ingestStats.equals(that.ingestStats) &&
                courseName.equals(that.courseName) &&
                fileName.equals(that.fileName) &&
                rootElementId.equals(that.rootElementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, workspaceId, configFields, ingestStats, courseName, fileName, rootElementId);
    }

    @Override
    public String toString() {
        return "ProjectIngestionRequestMessage{" +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                ", configFields='" + configFields + '\'' +
                ", ingestStats='" + ingestStats + '\'' +
                ", courseName='" + courseName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", rootElementId=" + rootElementId + '\'' +
                '}';
    }
}
