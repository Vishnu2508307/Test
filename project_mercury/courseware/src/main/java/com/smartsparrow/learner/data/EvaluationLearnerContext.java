package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

public class EvaluationLearnerContext implements EvaluationContext {

    private UUID deploymentId;
    private UUID studentId;

    public UUID getStudentId() {
        return studentId;
    }

    public EvaluationLearnerContext setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public EvaluationLearnerContext setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public Type getType() {
        return Type.LEARNER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationLearnerContext that = (EvaluationLearnerContext) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, deploymentId);
    }

    @Override
    public String toString() {
        return "EvaluationLearnerContext{" +
                "studentId=" + studentId +
                ", deploymentId=" + deploymentId +
                '}';
    }
}
