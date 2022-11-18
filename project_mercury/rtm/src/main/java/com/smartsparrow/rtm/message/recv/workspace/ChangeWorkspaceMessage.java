package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ChangeWorkspaceMessage extends ReceivedMessage implements WorkspaceMessage {

    private UUID workspaceId;
    private String name;
    private String description;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeWorkspaceMessage that = (ChangeWorkspaceMessage) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, name, description);
    }

    @Override
    public String toString() {
        return "ChangeWorkspaceMessage{" +
                "workspaceId=" + workspaceId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}
