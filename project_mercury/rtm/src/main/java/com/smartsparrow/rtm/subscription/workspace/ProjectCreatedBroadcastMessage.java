package com.smartsparrow.rtm.subscription.workspace;

import java.util.Objects;
import java.util.UUID;

public class ProjectCreatedBroadcastMessage extends WorkspaceBroadcastMessage {

    private static final long serialVersionUID = -4182306570526418384L;
    private final UUID projectId;

    public ProjectCreatedBroadcastMessage(UUID workspaceId, UUID projectId) {
        super(workspaceId);
        this.projectId = projectId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectCreatedBroadcastMessage that = (ProjectCreatedBroadcastMessage) o;
        return Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId);
    }

    @Override
    public String toString() {
        return "ProjectCreatedBroadcastMessage{" +
                "projectId=" + projectId +
                '}';
    }
}
