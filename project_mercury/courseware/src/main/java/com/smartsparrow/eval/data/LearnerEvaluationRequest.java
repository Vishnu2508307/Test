package com.smartsparrow.eval.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerWalkable;

public class LearnerEvaluationRequest implements EvaluationRequest {

    private LearnerWalkable learnerWalkable;
    private UUID studentId;
    private Attempt attempt;
    private UUID parentPathwayId;
    private String producingClientId;
    private ScenarioLifecycle scenarioLifecycle;
    private Deployment deployment;

    @Override
    public Type getType() {
        return Type.LEARNER;
    }

    public LearnerWalkable getLearnerWalkable() {
        return learnerWalkable;
    }

    public LearnerEvaluationRequest setLearnerWalkable(LearnerWalkable learnerWalkable) {
        this.learnerWalkable = learnerWalkable;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public LearnerEvaluationRequest setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public LearnerEvaluationRequest setAttempt(Attempt attempt) {
        this.attempt = attempt;
        return this;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    public LearnerEvaluationRequest setParentPathwayId(UUID parentPathwayId) {
        this.parentPathwayId = parentPathwayId;
        return this;
    }

    public String getProducingClientId() {
        return producingClientId;
    }

    public LearnerEvaluationRequest setProducingClientId(String producingClientId) {
        this.producingClientId = producingClientId;
        return this;
    }

    @Override
    public ScenarioLifecycle getScenarioLifecycle() {
        return scenarioLifecycle;
    }

    public LearnerEvaluationRequest setScenarioLifecycle(ScenarioLifecycle scenarioLifecycle) {
        this.scenarioLifecycle = scenarioLifecycle;
        return this;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public LearnerEvaluationRequest setDeployment(Deployment deployment) {
        this.deployment = deployment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerEvaluationRequest that = (LearnerEvaluationRequest) o;
        return Objects.equals(learnerWalkable, that.learnerWalkable) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(attempt, that.attempt) &&
                Objects.equals(parentPathwayId, that.parentPathwayId) &&
                Objects.equals(producingClientId, that.producingClientId) &&
                Objects.equals(scenarioLifecycle, that.scenarioLifecycle) &&
                Objects.equals(deployment, that.deployment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(learnerWalkable, studentId, attempt, parentPathwayId, producingClientId, scenarioLifecycle,
                deployment);
    }

    @Override
    public String toString() {
        return "LearnerEvaluationRequest{" +
                "learnerWalkable=" + learnerWalkable +
                ", studentId=" + studentId +
                ", attempt=" + attempt +
                ", parentPathwayId=" + parentPathwayId +
                ", producingClientId='" + producingClientId + '\'' +
                ", scenarioLifecycle='" + scenarioLifecycle + '\'' +
                ", deployment='" + deployment + '\'' +
                '}';
    }
}
