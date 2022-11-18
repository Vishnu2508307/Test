package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class StudentScope {

    private UUID id;
    private UUID deploymentId;
    private UUID accountId;
    private UUID scopeUrn;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public StudentScope setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public StudentScope setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getScopeUrn() {
        return scopeUrn;
    }

    public StudentScope setScopeUrn(UUID scopeUrn) {
        this.scopeUrn = scopeUrn;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public StudentScope setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScope that = (StudentScope) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(scopeUrn, that.scopeUrn) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, accountId, scopeUrn, id);
    }

    @Override
    public String toString() {
        return "StudentScope{" +
                "deploymentId=" + deploymentId +
                ", accountId=" + accountId +
                ", scopeUrn=" + scopeUrn +
                ", id=" + id +
                '}';
    }
}
