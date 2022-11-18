package com.smartsparrow.iam.data.permission.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamProjectPermission {

    private UUID projectId;
    private UUID teamId;
    private PermissionLevel permissionLevel;

    public UUID getProjectId() {
        return projectId;
    }

    public TeamProjectPermission setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public TeamProjectPermission setTeamId(final UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamProjectPermission setPermissionLevel(final PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamProjectPermission that = (TeamProjectPermission) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(teamId, that.teamId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, teamId, permissionLevel);
    }

    @Override
    public String toString() {
        return "ProjectTeamPermission{" +
                "projectId=" + projectId +
                ", teamId=" + teamId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
