package com.smartsparrow.learner.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

/**
 * Progress tracking on a LINEAR pathway.
 *
 * Tracks:
 *  - Walkable item IDs that have been seen/completed.
 *
 */
public class LinearPathwayProgress implements Progress {

    private static final long serialVersionUID = -6740401832769748268L;
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

    public LinearPathwayProgress() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public LinearPathwayProgress setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LinearPathwayProgress setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public LinearPathwayProgress setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public LinearPathwayProgress setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public LinearPathwayProgress setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public LinearPathwayProgress setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    public LinearPathwayProgress setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public LinearPathwayProgress setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public LinearPathwayProgress setCompletion(Completion completion) {
        this.completion = completion;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionValues() {
        return childWalkableCompletionValues;
    }

    public LinearPathwayProgress setChildWalkableCompletionValues(Map<UUID, Float> childWalkableCompletionValues) {
        this.childWalkableCompletionValues = childWalkableCompletionValues;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionConfidences() {
        return childWalkableCompletionConfidences;
    }

    public LinearPathwayProgress setChildWalkableCompletionConfidences(Map<UUID, Float> childWalkableCompletionConfidences) {
        this.childWalkableCompletionConfidences = childWalkableCompletionConfidences;
        return this;
    }

    public List<UUID> getCompletedWalkables() {
        return completedWalkables;
    }

    public LinearPathwayProgress setCompletedWalkables(List<UUID> completedWalkables) {
        this.completedWalkables = completedWalkables;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LinearPathwayProgress progress = (LinearPathwayProgress) o;
        return Objects.equal(id, progress.id) && Objects.equal(deploymentId, progress.deploymentId) && Objects.equal(
                changeId, progress.changeId) && Objects.equal(coursewareElementId, progress.coursewareElementId)
                && coursewareElementType == progress.coursewareElementType && Objects.equal(studentId,
                                                                                            progress.studentId)
                && Objects.equal(attemptId, progress.attemptId) && Objects.equal(evaluationId, progress.evaluationId)
                && Objects.equal(completion, progress.completion) && Objects.equal(childWalkableCompletionValues,
                                                                                   progress.childWalkableCompletionValues)
                && Objects.equal(childWalkableCompletionConfidences, progress.childWalkableCompletionConfidences)
                && Objects.equal(completedWalkables, progress.completedWalkables);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                                attemptId, evaluationId, completion, childWalkableCompletionValues,
                                childWalkableCompletionConfidences, completedWalkables);
    }

    @Override
    public String toString() {
        return "LinearPathwayProgress{" + "id=" + id + ", deploymentId=" + deploymentId + ", changeId=" + changeId
                + ", coursewareElementId=" + coursewareElementId + ", coursewareElementType=" + coursewareElementType
                + ", studentId=" + studentId + ", attemptId=" + attemptId + ", evaluationId=" + evaluationId
                + ", completion=" + completion + ", childWalkableCompletionValues=" + childWalkableCompletionValues
                + ", childWalkableCompletionConfidences=" + childWalkableCompletionConfidences + ", completedWalkables="
                + completedWalkables + '}';
    }
}
