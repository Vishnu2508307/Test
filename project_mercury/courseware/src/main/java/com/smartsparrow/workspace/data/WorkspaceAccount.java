package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class WorkspaceAccount {

    private UUID accountId;
    private UUID workspaceId;

    public UUID getAccountId() {
        return accountId;
    }

    public WorkspaceAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceAccount setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceAccount that = (WorkspaceAccount) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceAccount{" +
                "accountId=" + accountId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
