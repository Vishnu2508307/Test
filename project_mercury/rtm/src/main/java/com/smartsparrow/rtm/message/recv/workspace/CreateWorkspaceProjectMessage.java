package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateWorkspaceProjectMessage extends ReceivedMessage implements WorkspaceMessage {

    private UUID workspaceId;
    private String name;
    private String config;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getConfig() {
        return config;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateWorkspaceProjectMessage that = (CreateWorkspaceProjectMessage) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, name, config);
    }

    @Override
    public String toString() {
        return "CreateProjectMessage{" +
                "workspaceId=" + workspaceId +
                ", name='" + name + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
