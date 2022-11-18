package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamCohortCollaborator extends CohortCollaborator {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamCohortCollaborator setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public TeamCohortCollaborator setCohortId(UUID cohortId) {
        super.setCohortId(cohortId);
        return this;
    }

    @Override
    public TeamCohortCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamCohortCollaborator that = (TeamCohortCollaborator) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamId);
    }

    @Override
    public String toString() {
        return "TeamCohortCollaborator{" +
                "teamId=" + teamId +
                "} " + super.toString();
    }
}
