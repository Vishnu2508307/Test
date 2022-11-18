package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.mutation.MutationOperator;

public class ManualGradeEntry {

    private UUID deploymentId;
    private UUID studentId;
    private UUID componentId;
    private UUID attemptId;
    private UUID id;
    private Double maxScore;
    private Double score;
    private UUID changeId;
    private UUID parentId;
    private CoursewareElementType parentType;
    private MutationOperator operator;
    private UUID instructorId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public ManualGradeEntry setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public ManualGradeEntry setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public ManualGradeEntry setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public ManualGradeEntry setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public ManualGradeEntry setId(UUID id) {
        this.id = id;
        return this;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public ManualGradeEntry setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    public Double getScore() {
        return score;
    }

    public ManualGradeEntry setScore(Double score) {
        this.score = score;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public ManualGradeEntry setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public ManualGradeEntry setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    public ManualGradeEntry setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public ManualGradeEntry setOperator(MutationOperator operator) {
        this.operator = operator;
        return this;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public ManualGradeEntry setInstructorId(UUID instructorId) {
        this.instructorId = instructorId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualGradeEntry that = (ManualGradeEntry) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(componentId, that.componentId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(id, that.id) &&
                Objects.equals(maxScore, that.maxScore) &&
                Objects.equals(score, that.score) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType &&
                operator == that.operator &&
                Objects.equals(instructorId, that.instructorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, studentId, componentId, attemptId, id, maxScore, score, changeId, parentId,
                parentType, operator, instructorId);
    }

    @Override
    public String toString() {
        return "ManualGradeEntry{" +
                "deploymentId=" + deploymentId +
                ", studentId=" + studentId +
                ", componentId=" + componentId +
                ", attemptId=" + attemptId +
                ", id=" + id +
                ", maxScore=" + maxScore +
                ", score=" + score +
                ", changeId=" + changeId +
                ", parentId=" + parentId +
                ", parentType=" + parentType +
                ", operator=" + operator +
                ", instructorId=" + instructorId +
                '}';
    }
}
