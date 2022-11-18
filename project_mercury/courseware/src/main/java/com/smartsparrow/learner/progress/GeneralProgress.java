package com.smartsparrow.learner.progress;

import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class GeneralProgress implements Progress {

    private static final long serialVersionUID = -1693567019443855223L;
    private UUID id;
    private UUID deploymentId;
    private UUID changeId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType;
    private UUID studentId;
    private UUID attemptId;
    private UUID evaluationId;
    private Completion completion;

    public GeneralProgress() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public GeneralProgress setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public GeneralProgress setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public GeneralProgress setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public GeneralProgress setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public GeneralProgress setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public GeneralProgress setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    public GeneralProgress setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public GeneralProgress setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public GeneralProgress setCompletion(Completion completion) {
        this.completion = completion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GeneralProgress that = (GeneralProgress) o;
        return Objects.equal(id, that.id) && Objects.equal(deploymentId, that.deploymentId) && Objects.equal(changeId,
                                                                                                             that.changeId)
                && Objects.equal(coursewareElementId, that.coursewareElementId)
                && coursewareElementType == that.coursewareElementType && Objects.equal(studentId, that.studentId)
                && Objects.equal(attemptId, that.attemptId) && Objects.equal(evaluationId, that.evaluationId) && Objects
                .equal(completion, that.completion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                                attemptId, evaluationId, completion);
    }

    @Override
    public String toString() {
        return "GeneralProgress{" + "id=" + id + ", deploymentId=" + deploymentId + ", changeId=" + changeId
                + ", coursewareElementId=" + coursewareElementId + ", coursewareElementType=" + coursewareElementType
                + ", studentId=" + studentId + ", attemptId=" + attemptId + ", evaluationId=" + evaluationId
                + ", completion=" + completion + '}';
    }
}
