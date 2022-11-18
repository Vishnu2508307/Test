package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LearnerSearchableDocumentIdentity {

    private UUID deploymentId;

    private UUID elementId;

    private UUID searchableFieldId;

    private UUID changeId;

    /**
     * CSG index id, it is composed of other ids in the pattern deploymentId
     */
    public String getId() {
        return getDeploymentId() + ":" + getElementId();
    }

    /**
     * ---> IMPORTANT!!! <---
     * Equality in this pojo ignores changeId.
     * ---> IMPORTANT!!! <---
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerSearchableDocumentIdentity that = (LearnerSearchableDocumentIdentity) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(searchableFieldId, that.searchableFieldId);
    }

    /**
     * ---> IMPORTANT!!! <---
     * HashCode in this pojo ignores changeId.
     * ---> IMPORTANT!!! <---
     */
    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, elementId, searchableFieldId);
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerSearchableDocumentIdentity setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public LearnerSearchableDocumentIdentity setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getSearchableFieldId() {
        return searchableFieldId;
    }

    public LearnerSearchableDocumentIdentity setSearchableFieldId(final UUID searchableFieldId) {
        this.searchableFieldId = searchableFieldId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerSearchableDocumentIdentity setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public String toString() {
        return "LearnerSearchableDocumentIdentity{" +
                "deploymentId=" + deploymentId +
                ", elementId=" + elementId +
                ", searchableDocumentId=" + searchableFieldId +
                ", changeId=" + changeId +
                '}';
    }
}

