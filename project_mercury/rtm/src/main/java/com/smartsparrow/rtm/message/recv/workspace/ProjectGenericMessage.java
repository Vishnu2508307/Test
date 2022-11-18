package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectGenericMessage extends ReceivedMessage implements ProjectMessage {

    private UUID projectId;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectGenericMessage that = (ProjectGenericMessage) o;
        return Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    @Override
    public String toString() {
        return "GenericProjectMessage{" +
                "projectId=" + projectId +
                "} " + super.toString();
    }
}
