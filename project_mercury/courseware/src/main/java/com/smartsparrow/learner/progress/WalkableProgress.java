package com.smartsparrow.learner.progress;

import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

/**
 * Provide progress tracking to Walkable objects.
 */
public class WalkableProgress implements Progress {

    private static final long serialVersionUID = 5909776708322833866L;
    //
    private UUID id;
    private UUID deploymentId;
    private UUID changeId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType;
    private UUID studentId;
    private UUID attemptId;
    private UUID evaluationId;
    private Completion completion;

    public WalkableProgress() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public WalkableProgress setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public WalkableProgress setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public WalkableProgress setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public WalkableProgress setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public WalkableProgress setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public WalkableProgress setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    public WalkableProgress setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public WalkableProgress setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public WalkableProgress setCompletion(Completion completion) {
        this.completion = completion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WalkableProgress that = (WalkableProgress) o;
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
        return "WalkableProgress{" + "id=" + id + ", deploymentId=" + deploymentId + ", changeId=" + changeId
                + ", coursewareElementId=" + coursewareElementId + ", coursewareElementType=" + coursewareElementType
                + ", studentId=" + studentId + ", attemptId=" + attemptId + ", evaluationId=" + evaluationId
                + ", completion=" + completion + '}';
    }
}
