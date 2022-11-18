package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class LearnerManualGradingConfiguration {

    private UUID deploymentId;
    private UUID changeId;
    private UUID componentId;
    private Double maxScore;
    private UUID parentId;
    private CoursewareElementType parentType;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerManualGradingConfiguration setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    @JsonIgnore
    public UUID getChangeId() {
        return changeId;
    }

    public LearnerManualGradingConfiguration setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public LearnerManualGradingConfiguration setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    @Nullable
    public Double getMaxScore() {
        return maxScore;
    }

    public LearnerManualGradingConfiguration setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public LearnerManualGradingConfiguration setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public LearnerManualGradingConfiguration setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerManualGradingConfiguration that = (LearnerManualGradingConfiguration) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(componentId, that.componentId) &&
                Objects.equals(maxScore, that.maxScore) &&
                Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, changeId, componentId, maxScore, parentId, parentType);
    }

    @Override
    public String toString() {
        return "LearnerManualGradingConfiguration{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", componentId=" + componentId +
                ", maxScore=" + maxScore +
                ", parentId=" + parentId +
                ", parentType=" + parentType +
                '}';
    }
}
