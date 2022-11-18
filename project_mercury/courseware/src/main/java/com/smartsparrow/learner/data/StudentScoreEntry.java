package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.mutation.MutationOperator;

public class StudentScoreEntry {

    private UUID id;
    private UUID cohortId;
    private UUID deploymentId;
    private UUID changeId;
    private UUID studentId;
    private UUID elementId;
    private UUID attemptId;
    private Double value;
    private Double adjustmentValue;
    private UUID evaluationId;
    private MutationOperator operator;
    private UUID sourceElementId;
    private UUID sourceScenarioId;
    private UUID sourceAccountId;
    private CoursewareElementType elementType;

    public UUID getId() {
        return id;
    }

    public StudentScoreEntry setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public StudentScoreEntry setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public StudentScoreEntry setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public StudentScoreEntry setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public StudentScoreEntry setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public StudentScoreEntry setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public StudentScoreEntry setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public Double getValue() {
        return value;
    }

    public StudentScoreEntry setValue(Double value) {
        this.value = value;
        return this;
    }

    public Double getAdjustmentValue() {
        return adjustmentValue;
    }

    public StudentScoreEntry setAdjustmentValue(Double adjustmentValue) {
        this.adjustmentValue = adjustmentValue;
        return this;
    }

    @Nullable
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public StudentScoreEntry setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public StudentScoreEntry setOperator(MutationOperator operator) {
        this.operator = operator;
        return this;
    }

    @Nullable
    public UUID getSourceElementId() {
        return sourceElementId;
    }

    public StudentScoreEntry setSourceElementId(UUID sourceElementId) {
        this.sourceElementId = sourceElementId;
        return this;
    }

    @Nullable
    public UUID getSourceScenarioId() {
        return sourceScenarioId;
    }

    public StudentScoreEntry setSourceScenarioId(UUID sourceScenarioId) {
        this.sourceScenarioId = sourceScenarioId;
        return this;
    }

    @Nullable
    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public StudentScoreEntry setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public StudentScoreEntry setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScoreEntry that = (StudentScoreEntry) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(adjustmentValue, that.adjustmentValue) &&
                Objects.equals(evaluationId, that.evaluationId) &&
                operator == that.operator &&
                Objects.equals(sourceElementId, that.sourceElementId) &&
                Objects.equals(sourceScenarioId, that.sourceScenarioId) &&
                Objects.equals(sourceAccountId, that.sourceAccountId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cohortId, deploymentId, changeId, studentId, elementId, attemptId, value,
                adjustmentValue, evaluationId, operator, sourceElementId, sourceScenarioId, sourceAccountId, elementType);
    }

    @Override
    public String toString() {
        return "StudentScoreEntry{" +
                "id=" + id +
                ", cohortId=" + cohortId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", studentId=" + studentId +
                ", elementId=" + elementId +
                ", attemptId=" + attemptId +
                ", value=" + value +
                ", adjustmentValue=" + adjustmentValue +
                ", evaluationId=" + evaluationId +
                ", operator=" + operator +
                ", sourceElementId=" + sourceElementId +
                ", sourceScenarioId=" + sourceScenarioId +
                ", sourceAccountId=" + sourceAccountId +
                ", elementType=" + elementType +
                '}';
    }
}
