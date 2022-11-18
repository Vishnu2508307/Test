package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class CohortCollaborator {

    private UUID cohortId;
    private PermissionLevel permissionLevel;

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortCollaborator setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public CohortCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortCollaborator that = (CohortCollaborator) o;
        return Objects.equals(cohortId, that.cohortId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortId, permissionLevel);
    }

    @Override
    public String toString() {
        return "CohortCollaborator{" +
                "cohortId=" + cohortId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
