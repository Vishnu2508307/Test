package com.smartsparrow.rtm.message.recv.workspace;

import com.smartsparrow.rtm.message.ReceivedMessage;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectActivityListMessage extends ReceivedMessage implements ProjectMessage {

    private UUID projectId;
    private List<String> fieldNames;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectActivityListMessage that = (ProjectActivityListMessage) o;
        return Objects.equals(projectId, that.projectId) &&
        Objects.equals(fieldNames, that.fieldNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    @Override
    public String toString() {
        return "GenericProjectMessage{" +
                "projectId=" + projectId +
                "fieldNames=" +fieldNames+
                "} " + super.toString();
    }
}
