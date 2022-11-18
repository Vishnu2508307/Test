package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class ProjectAccountCollaborator extends ProjectCollaborator {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public ProjectAccountCollaborator setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public ProjectAccountCollaborator setProjectId(final UUID projectId) {
        super.setProjectId(projectId);
        return this;
    }

    @Override
    public ProjectAccountCollaborator setPermissionLevel(final PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectAccountCollaborator that = (ProjectAccountCollaborator) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "ProjectAccountCollaborator{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
