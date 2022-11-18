package com.smartsparrow.learner.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class FreePathwayProgress implements Progress {

    private static final long serialVersionUID = -7075466852086669396L;
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
    private List<UUID> completedWalkables = new ArrayList<>();

    public FreePathwayProgress() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public FreePathwayProgress setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public FreePathwayProgress setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public FreePathwayProgress setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public FreePathwayProgress setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public FreePathwayProgress setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public FreePathwayProgress setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    public FreePathwayProgress setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public FreePathwayProgress setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public FreePathwayProgress setCompletion(Completion completion) {
        this.completion = completion;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionValues() {
        return childWalkableCompletionValues;
    }

    public FreePathwayProgress setChildWalkableCompletionValues(Map<UUID, Float> childWalkableCompletionValues) {
        this.childWalkableCompletionValues = childWalkableCompletionValues;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionConfidences() {
        return childWalkableCompletionConfidences;
    }

    public FreePathwayProgress setChildWalkableCompletionConfidences(Map<UUID, Float> childWalkableCompletionConfidences) {
        this.childWalkableCompletionConfidences = childWalkableCompletionConfidences;
        return this;
    }

    public List<UUID> getCompletedWalkables() {
        return completedWalkables;
    }

    public FreePathwayProgress setCompletedWalkables(List<UUID> completedWalkables) {
        this.completedWalkables = completedWalkables;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FreePathwayProgress that = (FreePathwayProgress) o;
        return Objects.equal(id, that.id) && Objects.equal(deploymentId, that.deploymentId) && Objects.equal(changeId,
                                                                                                             that.changeId)
                && Objects.equal(coursewareElementId, that.coursewareElementId)
                && coursewareElementType == that.coursewareElementType && Objects.equal(studentId, that.studentId)
                && Objects.equal(attemptId, that.attemptId) && Objects.equal(evaluationId, that.evaluationId) && Objects
                .equal(completion, that.completion) && Objects.equal(childWalkableCompletionValues,
                                                                     that.childWalkableCompletionValues)
                && Objects.equal(childWalkableCompletionConfidences, that.childWalkableCompletionConfidences) && Objects
                .equal(completedWalkables, that.completedWalkables);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                                attemptId, evaluationId, completion, childWalkableCompletionValues,
                                childWalkableCompletionConfidences, completedWalkables);
    }

    @Override
    public String toString() {
        return "FreePathwayProgress{" + "id=" + id + ", deploymentId=" + deploymentId + ", changeId=" + changeId
                + ", coursewareElementId=" + coursewareElementId + ", coursewareElementType=" + coursewareElementType
                + ", studentId=" + studentId + ", attemptId=" + attemptId + ", evaluationId=" + evaluationId
                + ", completion=" + completion + ", childWalkableCompletionValues=" + childWalkableCompletionValues
                + ", childWalkableCompletionConfidences=" + childWalkableCompletionConfidences + ", completedWalkables="
                + completedWalkables + '}';
    }
}
