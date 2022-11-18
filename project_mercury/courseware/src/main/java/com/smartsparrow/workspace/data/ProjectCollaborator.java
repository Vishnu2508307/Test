package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class ProjectCollaborator {

    private UUID projectId;
    private PermissionLevel permissionLevel;

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectCollaborator setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public ProjectCollaborator setPermissionLevel(final PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectCollaborator that = (ProjectCollaborator) o;
        return Objects.equals(projectId, that.projectId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, permissionLevel);
    }

    @Override
    public String toString() {
        return "ProjectCollaborator{" +
                "projectId=" + projectId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
