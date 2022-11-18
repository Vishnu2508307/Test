package com.smartsparrow.learner.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.learner.attempt.Attempt;

public class RestartActivityEventMessage implements UpdateProgressMessage {

    private UUID studentId;
    private String producingClientId;
    private UUID attemptId;
    private UUID changeId;
    private UUID deploymentId;
    private Attempt attempt;
    private List<CoursewareElement> ancestryList = new ArrayList<>();

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    @Override
    public String getProducingClientId() {
        return producingClientId;
    }

    @Override
    public List<CoursewareElement> getAncestryList() {
        return ancestryList;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public UUID getEvaluationId() {
        return null;
    }

    @Override
    public Attempt getAttempt() {
        return attempt;
    }

    public RestartActivityEventMessage setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public RestartActivityEventMessage setProducingClientId(String producingClientId) {
        this.producingClientId = producingClientId;
        return this;
    }

    public RestartActivityEventMessage setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public RestartActivityEventMessage setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public RestartActivityEventMessage setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public RestartActivityEventMessage setAttempt(Attempt attempt) {
        this.attempt = attempt;
        return this;
    }

    public RestartActivityEventMessage setAncestryList(List<CoursewareElement> ancestryList) {
        this.ancestryList = ancestryList;
        return this;
    }
}
