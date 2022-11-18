package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class WorkspaceTeamCollaborator extends WorkspaceCollaborator {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public WorkspaceTeamCollaborator setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public WorkspaceTeamCollaborator setWorkspaceId(UUID workspaceId) {
        super.setWorkspaceId(workspaceId);
        return this;
    }

    @Override
    public WorkspaceTeamCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkspaceTeamCollaborator that = (WorkspaceTeamCollaborator) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamId);
    }

    @Override
    public String toString() {
        return "WorkspaceTeamCollaborator{" +
                "teamId=" + teamId +
                "} " + super.toString();
    }
}
