package com.smartsparrow.learner.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class Deployment implements Serializable {

    private static final long serialVersionUID = -6156212981592678668L;

    private UUID id;
    private UUID changeId;
    private UUID cohortId;

    public UUID getId() {
        return id;
    }

    public Deployment setId(UUID id) {
        this.id = id;
        return this;
    }

    @GraphQLIgnore //for inner usage only
    public UUID getChangeId() {
        return changeId;
    }

    public Deployment setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public Deployment setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deployment that = (Deployment) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, changeId, cohortId);
    }

    @Override
    public String toString() {
        return "Deployment{" +
                "id=" + id +
                ", changeId=" + changeId +
                ", cohortId=" + cohortId +
                '}';
    }
}
