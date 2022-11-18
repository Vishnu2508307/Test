package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class DeletedWorkspace {

    private UUID workspaceId;
    private String name;
    private UUID accountId;
    private String deletedAt;

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public DeletedWorkspace setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getName() {
        return name;
    }

    public DeletedWorkspace setName(String name) {
        this.name = name;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DeletedWorkspace setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public DeletedWorkspace setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletedWorkspace that = (DeletedWorkspace) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(deletedAt, that.deletedAt);
    }

    @Override
    public int hashCode() {

        return Objects.hash(workspaceId, name, accountId, deletedAt);
    }

    @Override
    public String toString() {
        return "DeletedWorkspace{" +
                "workspaceId=" + workspaceId +
                ", name=" + name +
                ", accountId=" + accountId +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
