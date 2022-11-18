package com.smartsparrow.learner.event;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.EvaluationResult;

@Deprecated
public class EvaluationEventMessage implements UpdateProgressMessage {

    private EvaluationResult evaluationResult;
    private List<CoursewareElement> ancestryList;
    private UUID studentId; // current student who triggered the evaluation
    private String producingClientId; // rtm client id which triggered the evaluation
    private Deployment deployment;
    private EvaluationActionState evaluationActionState;

    public EvaluationEventMessage() {
    }

    public EvaluationActionState getEvaluationActionState() { return evaluationActionState; }

    public EvaluationEventMessage setEvaluationActionState(final EvaluationActionState evaluationActionState) {
        this.evaluationActionState = evaluationActionState;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return evaluationResult.getAttemptId();
    }

    @Override
    public UUID getChangeId() {
        return evaluationResult.getDeployment().getChangeId();
    }

    @Override
    public UUID getDeploymentId() {
        return evaluationResult.getDeployment().getId();
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationResult.getId();
    }

    @Override
    public Attempt getAttempt() {
        return evaluationResult.getAttempt();
    }

    public EvaluationResult getEvaluationResult() {
        return evaluationResult;
    }

    public EvaluationEventMessage setEvaluationResult(EvaluationResult evaluationResult) {
        this.evaluationResult = evaluationResult;
        return this;
    }

    public Deployment getDeployment() { return deployment; }

    public EvaluationEventMessage setDeployment(Deployment deployment) {
        this.deployment = deployment;
        return this;
    }

    @Override
    public List<CoursewareElement> getAncestryList() {
        return ancestryList;
    }

    public EvaluationEventMessage setAncestryList(List<CoursewareElement> ancestryList) {
        this.ancestryList = ancestryList;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public EvaluationEventMessage setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public String getProducingClientId() {
        return producingClientId;
    }

    public EvaluationEventMessage setProducingClientId(String producingClientId) {
        this.producingClientId = producingClientId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationEventMessage that = (EvaluationEventMessage) o;
        return Objects.equals(evaluationResult, that.evaluationResult) &&
                Objects.equals(ancestryList, that.ancestryList) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(producingClientId, that.producingClientId) &&
                Objects.equals(deployment, that.deployment) &&
                Objects.equals(evaluationActionState, that.evaluationActionState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationResult, ancestryList, studentId, producingClientId, deployment, evaluationActionState);
    }

    @Override
    public String toString() {
        return "EvaluationEventMessage{" +
                "evaluationResult=" + evaluationResult +
                ", ancestryList=" + ancestryList +
                ", studentId=" + studentId +
                ", producingClientId='" + producingClientId + '\'' +
                ", deployment=" + deployment +
                ", evaluationActionState=" + evaluationActionState +
                '}';
    }
}
