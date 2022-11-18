package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.workspace.ProjectMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectIngestionListMessage extends ReceivedMessage implements ProjectMessage {

    private UUID projectId;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public ProjectIngestionListMessage setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectIngestionListMessage that = (ProjectIngestionListMessage) o;
        return projectId.equals(that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    @Override
    public String toString() {
        return "ProjectIngestionListMessage{" +
                ", projectId=" + projectId +
                '}';
    }
}
