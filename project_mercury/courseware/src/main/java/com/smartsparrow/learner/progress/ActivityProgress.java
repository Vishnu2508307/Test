package com.smartsparrow.learner.progress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class ActivityProgress implements Progress {
    private static final long serialVersionUID = -905160861843968997L;
    //
    private UUID id;
    private UUID deploymentId;
    private UUID changeId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType = CoursewareElementType.PATHWAY;
    private UUID studentId;
    private UUID attemptId;
    private UUID evaluationId;
    private Completion completion;

    //
    private Map<UUID, Float> childWalkableCompletionValues = new HashMap<>();
    private Map<UUID, Float> childWalkableCompletionConfidences = new HashMap<>();

    public ActivityProgress() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public ActivityProgress setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public ActivityProgress setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public ActivityProgress setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public ActivityProgress setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public ActivityProgress setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public ActivityProgress setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    public ActivityProgress setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public ActivityProgress setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public ActivityProgress setCompletion(Completion completion) {
        this.completion = completion;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionValues() {
        return childWalkableCompletionValues;
    }

    public ActivityProgress setChildWalkableCompletionValues(Map<UUID, Float> childWalkableCompletionValues) {
        this.childWalkableCompletionValues = childWalkableCompletionValues;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionConfidences() {
        return childWalkableCompletionConfidences;
    }

    public ActivityProgress setChildWalkableCompletionConfidences(Map<UUID, Float> childWalkableCompletionConfidences) {
        this.childWalkableCompletionConfidences = childWalkableCompletionConfidences;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActivityProgress that = (ActivityProgress) o;
        return Objects.equal(id, that.id) && Objects.equal(deploymentId, that.deploymentId) && Objects.equal(changeId,
                                                                                                             that.changeId)
                && Objects.equal(coursewareElementId, that.coursewareElementId)
                && coursewareElementType == that.coursewareElementType && Objects.equal(studentId, that.studentId)
                && Objects.equal(attemptId, that.attemptId) && Objects.equal(evaluationId, that.evaluationId) && Objects
                .equal(completion, that.completion) && Objects.equal(childWalkableCompletionValues,
                                                                     that.childWalkableCompletionValues)
                && Objects.equal(childWalkableCompletionConfidences, that.childWalkableCompletionConfidences);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                                attemptId, evaluationId, completion, childWalkableCompletionValues,
                                childWalkableCompletionConfidences);
    }

    @Override
    public String toString() {
        return "ActivityProgress{" + "id=" + id + ", deploymentId=" + deploymentId + ", changeId=" + changeId
                + ", coursewareElementId=" + coursewareElementId + ", coursewareElementType=" + coursewareElementType
                + ", studentId=" + studentId + ", attemptId=" + attemptId + ", evaluationId=" + evaluationId
                + ", completion=" + completion + ", childWalkableCompletionValues=" + childWalkableCompletionValues
                + ", childWalkableCompletionConfidences=" + childWalkableCompletionConfidences + '}';
    }
}
