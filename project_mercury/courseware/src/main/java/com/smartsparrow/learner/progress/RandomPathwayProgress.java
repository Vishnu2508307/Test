package com.smartsparrow.learner.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerRandomPathway;

public class RandomPathwayProgress implements Progress {

    private static final long serialVersionUID = 2947753275795572793L;
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
    private UUID inProgressElementId;
    private CoursewareElementType inProgressElementType;

    //
    private Map<UUID, Float> childWalkableCompletionValues = new HashMap<>();
    private Map<UUID, Float> childWalkableCompletionConfidences = new HashMap<>();
    private List<UUID> completedWalkables = new ArrayList<>();

    /**
     * When a student attempts a walkable with no success, the next time they access the random pathway they should be
     * presented with the walkable in progress. This method is used to determine which walkable should be shown to
     * the student when the {@link LearnerRandomPathway#supplyRelevantWalkables(UUID)} is invoked.
     *
     * @return the in progress element for this pathway
     */
    @Nullable
    public UUID getInProgressElementId() {
        return inProgressElementId;
    }

    public RandomPathwayProgress setInProgressElementId(final UUID inProgressElementId) {
        this.inProgressElementId = inProgressElementId;
        return this;
    }

    /**
     * @return the in progress element type for this pathway
     */
    @Nullable
    public CoursewareElementType getInProgressElementType() {
        return inProgressElementType;
    }

    public RandomPathwayProgress setInProgressElementType(final CoursewareElementType inProgressElementType) {
        this.inProgressElementType = inProgressElementType;
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public RandomPathwayProgress setId(final UUID id) {
        this.id = id;
        return this;
    }

    public RandomPathwayProgress setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public RandomPathwayProgress setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public RandomPathwayProgress setCoursewareElementId(final UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public RandomPathwayProgress setCoursewareElementType(final CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    public RandomPathwayProgress setStudentId(final UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public RandomPathwayProgress setAttemptId(final UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public RandomPathwayProgress setEvaluationId(final UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    public RandomPathwayProgress setCompletion(final Completion completion) {
        this.completion = completion;
        return this;
    }

    public RandomPathwayProgress setChildWalkableCompletionValues(final Map<UUID, Float> childWalkableCompletionValues) {
        this.childWalkableCompletionValues = childWalkableCompletionValues;
        return this;
    }

    public RandomPathwayProgress setChildWalkableCompletionConfidences(final Map<UUID, Float> childWalkableCompletionConfidences) {
        this.childWalkableCompletionConfidences = childWalkableCompletionConfidences;
        return this;
    }

    public RandomPathwayProgress setCompletedWalkables(final List<UUID> completedWalkables) {
        this.completedWalkables = completedWalkables;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionValues() {
        return childWalkableCompletionValues;
    }

    public Map<UUID, Float> getChildWalkableCompletionConfidences() {
        return childWalkableCompletionConfidences;
    }

    public List<UUID> getCompletedWalkables() {
        return completedWalkables;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomPathwayProgress that = (RandomPathwayProgress) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                coursewareElementType == that.coursewareElementType &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(completion, that.completion) &&
                Objects.equals(childWalkableCompletionValues, that.childWalkableCompletionValues) &&
                Objects.equals(childWalkableCompletionConfidences, that.childWalkableCompletionConfidences) &&
                Objects.equals(completedWalkables, that.completedWalkables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                attemptId, evaluationId, completion, childWalkableCompletionValues,
                childWalkableCompletionConfidences, completedWalkables);
    }

    @Override
    public String toString() {
        return "RandomPathwayProgress{" +
                "id=" + id +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", coursewareElementId=" + coursewareElementId +
                ", coursewareElementType=" + coursewareElementType +
                ", studentId=" + studentId +
                ", attemptId=" + attemptId +
                ", evaluationId=" + evaluationId +
                ", completion=" + completion +
                ", childWalkableCompletionValues=" + childWalkableCompletionValues +
                ", childWalkableCompletionConfidences=" + childWalkableCompletionConfidences +
                ", completedWalkables=" + completedWalkables +
                '}';
    }
}
