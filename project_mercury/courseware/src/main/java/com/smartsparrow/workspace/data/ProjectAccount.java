package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ProjectAccount {

    private UUID projectId;
    private UUID accountId;
    private UUID workspaceId;

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectAccount setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public ProjectAccount setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ProjectAccount setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectAccount that = (ProjectAccount) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, accountId, workspaceId);
    }

    @Override
    public String toString() {
        return "ProjectAccount{" +
                "projectId=" + projectId +
                ", accountId=" + accountId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
