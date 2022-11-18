package com.smartsparrow.iam.data.permission.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamCohortPermission {

    private UUID teamId;
    private UUID cohortId;
    private PermissionLevel permissionLevel;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamCohortPermission setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public TeamCohortPermission setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamCohortPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamCohortPermission that = (TeamCohortPermission) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(cohortId, that.cohortId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(teamId, cohortId, permissionLevel);
    }

    @Override
    public String toString() {
        return "TeamCohortPermission{" +
                "teamId=" + teamId +
                ", cohortId=" + cohortId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
