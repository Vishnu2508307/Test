package com.smartsparrow.iam.collaborator;

import java.util.Objects;

public class CollaboratorResult {

    private Long total;
    private Collaborators collaborators;

    public Long getTotal() {
        return total;
    }

    public CollaboratorResult setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Collaborators getCollaborators() {
        return collaborators;
    }

    public CollaboratorResult setCollaborators(Collaborators collaborators) {
        this.collaborators = collaborators;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollaboratorResult that = (CollaboratorResult) o;
        return Objects.equals(total, that.total) &&
                Objects.equals(collaborators, that.collaborators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, collaborators);
    }

    @Override
    public String toString() {
        return "CollaboratorResult{" +
                "total=" + total +
                ", collaborators=" + collaborators +
                '}';
    }
}
