package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class WorkspaceProject {

    private UUID projectId;
    private UUID workspaceId;

    public UUID getProjectId() {
        return projectId;
    }

    public WorkspaceProject setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceProject setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceProject that = (WorkspaceProject) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceProject{" +
                "projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
