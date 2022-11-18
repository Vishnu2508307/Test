package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class WorkspaceAccountCollaborator extends WorkspaceCollaborator {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public WorkspaceAccountCollaborator setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public WorkspaceAccountCollaborator setWorkspaceId(UUID workspaceId) {
        super.setWorkspaceId(workspaceId);
        return this;
    }

    @Override
    public WorkspaceAccountCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkspaceAccountCollaborator that = (WorkspaceAccountCollaborator) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "WorkspaceAccountCollaborator{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
