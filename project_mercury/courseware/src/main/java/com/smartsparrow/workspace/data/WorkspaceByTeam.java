package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class WorkspaceByTeam {

    private UUID teamId;
    private UUID workspaceId;

    public UUID getTeamId() {
        return teamId;
    }

    public WorkspaceByTeam setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceByTeam setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceByTeam that = (WorkspaceByTeam) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceByTeam{" +
                "teamId=" + teamId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
