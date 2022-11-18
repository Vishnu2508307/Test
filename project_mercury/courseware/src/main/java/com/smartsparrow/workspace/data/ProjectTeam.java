package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ProjectTeam {

    private UUID projectId;
    private UUID teamId;
    private UUID workspaceId;

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectTeam setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public ProjectTeam setTeamId(final UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ProjectTeam setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectTeam that = (ProjectTeam) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, teamId, workspaceId);
    }

    @Override
    public String toString() {
        return "ProjectTeam{" +
                "projectId=" + projectId +
                ", teamId=" + teamId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
