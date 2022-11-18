package com.smartsparrow.iam.data.permission.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamWorkspacePermission {

    private UUID teamId;
    private UUID workspaceId;
    private PermissionLevel permissionLevel;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamWorkspacePermission setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public TeamWorkspacePermission setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamWorkspacePermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamWorkspacePermission that = (TeamWorkspacePermission) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, workspaceId, permissionLevel);
    }

    @Override
    public String toString() {
        return "TeamWorkspacePermission{" +
                "teamId=" + teamId +
                ", workspaceId=" + workspaceId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
