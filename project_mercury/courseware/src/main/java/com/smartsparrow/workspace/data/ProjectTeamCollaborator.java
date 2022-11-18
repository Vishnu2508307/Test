package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class ProjectTeamCollaborator extends ProjectCollaborator {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public ProjectTeamCollaborator setTeamId(final UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public ProjectTeamCollaborator setProjectId(final UUID projectId) {
        super.setProjectId(projectId);
        return this;
    }

    @Override
    public ProjectTeamCollaborator setPermissionLevel(final PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectTeamCollaborator that = (ProjectTeamCollaborator) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamId);
    }

    @Override
    public String toString() {
        return "ProjectTeamCollaborator{" +
                "teamId=" + teamId +
                "} " + super.toString();
    }
}
