package com.smartsparrow.workspace.subscription;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.ingestion.data.IngestionStatus;

public class ProjectBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = 213297486152275119L;

    private UUID projectId;
    private UUID ingestionId;
    private IngestionStatus ingestionStatus;

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectBroadcastMessage setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getIngestionId() {
        return ingestionId;
    }

    public ProjectBroadcastMessage setIngestionId(UUID ingestionId) {
        this.ingestionId = ingestionId;
        return this;
    }

    public IngestionStatus getIngestionStatus() {
        return ingestionStatus;
    }

    public ProjectBroadcastMessage setIngestionStatus(IngestionStatus ingestionStatus) {
        this.ingestionStatus = ingestionStatus;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectBroadcastMessage that = (ProjectBroadcastMessage) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(ingestionId, that.ingestionId) &&
                Objects.equals(ingestionStatus, that.ingestionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, ingestionId, ingestionStatus);
    }

    @Override
    public String toString() {
        return "ProjectBroadcastMessage{" +
                "projectId=" + projectId +
                ", ingestionId= " + ingestionId +
                ", ingestionStatus= " + ingestionStatus +
                '}';
    }
}
