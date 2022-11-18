package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class CompletedWalkable {

    private UUID deploymentId;
    private UUID changeId;
    private UUID studentId;
    private UUID parentElementId;
    private UUID parentElementAttemptId;
    private UUID elementId;
    private UUID evaluationId;
    private UUID elementAttemptId;
    private CoursewareElementType parentElementType;
    private CoursewareElementType elementType;
    private String evaluatedAt;

    @GraphQLIgnore
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public CompletedWalkable setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getChangeId() {
        return changeId;
    }

    public CompletedWalkable setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public CompletedWalkable setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getParentElementId() {
        return parentElementId;
    }

    public CompletedWalkable setParentElementId(UUID parentElementId) {
        this.parentElementId = parentElementId;
        return this;
    }

    public UUID getParentElementAttemptId() {
        return parentElementAttemptId;
    }

    public CompletedWalkable setParentElementAttemptId(UUID parentElementAttemptId) {
        this.parentElementAttemptId = parentElementAttemptId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CompletedWalkable setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getEvaluationId() {
        return evaluationId;
    }

    public CompletedWalkable setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    public UUID getElementAttemptId() {
        return elementAttemptId;
    }

    public CompletedWalkable setElementAttemptId(UUID elementAttemptId) {
        this.elementAttemptId = elementAttemptId;
        return this;
    }

    @GraphQLIgnore
    public CoursewareElementType getParentElementType() {
        return parentElementType;
    }

    public CompletedWalkable setParentElementType(CoursewareElementType parentElementType) {
        this.parentElementType = parentElementType;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public CompletedWalkable setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public String getEvaluatedAt() {
        return evaluatedAt;
    }

    public CompletedWalkable setEvaluatedAt(String evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletedWalkable that = (CompletedWalkable) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(parentElementId, that.parentElementId) &&
                Objects.equals(parentElementAttemptId, that.parentElementAttemptId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(elementAttemptId, that.elementAttemptId) &&
                parentElementType == that.parentElementType &&
                elementType == that.elementType &&
                Objects.equals(evaluatedAt, that.evaluatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, changeId, studentId, parentElementId, parentElementAttemptId, elementId,
                evaluationId, elementAttemptId, parentElementType, elementType, evaluatedAt);
    }

    @Override
    public String toString() {
        return "CompletedWalkable{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", studentId=" + studentId +
                ", parentElementId=" + parentElementId +
                ", parentElementAttemptId=" + parentElementAttemptId +
                ", elementId=" + elementId +
                ", evaluationId=" + evaluationId +
                ", elementAttemptId=" + elementAttemptId +
                ", parentElementType=" + parentElementType +
                ", elementType=" + elementType +
                ", evaluatedAt='" + evaluatedAt + '\'' +
                '}';
    }
}
