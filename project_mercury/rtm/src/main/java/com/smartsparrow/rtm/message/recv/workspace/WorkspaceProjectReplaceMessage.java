package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class WorkspaceProjectReplaceMessage extends ReceivedMessage implements ProjectMessage {

    private String name;
    private UUID projectId;
    private String config;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public String getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceProjectReplaceMessage that = (WorkspaceProjectReplaceMessage) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, projectId, config);
    }

    @Override
    public String toString() {
        return "WorkspaceProjectReplaceMessage{" +
                "name='" + name + '\'' +
                ", projectId=" + projectId +
                ", config='" + config + '\'' +
                "} " + super.toString();
    }
}
