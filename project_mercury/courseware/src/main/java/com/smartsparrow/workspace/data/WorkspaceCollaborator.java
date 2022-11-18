package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class WorkspaceCollaborator {

    private UUID workspaceId;
    private PermissionLevel permissionLevel;

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceCollaborator setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public WorkspaceCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceCollaborator that = (WorkspaceCollaborator) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, permissionLevel);
    }

    @Override
    public String toString() {
        return "WorkspaceCollaborator{" +
                "workspaceId=" + workspaceId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
