package com.smartsparrow.learner.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class GraphPathwayProgress implements Progress {

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
    private UUID currentWalkableId;
    private CoursewareElementType currentWalkableType;

    //
    private Map<UUID, Float> childWalkableCompletionValues = new HashMap<>();
    private Map<UUID, Float> childWalkableCompletionConfidences = new HashMap<>();
    private List<UUID> completedWalkables = new ArrayList<>();

    @Override
    public UUID getId() {
        return id;
    }

    public GraphPathwayProgress setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public GraphPathwayProgress setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public GraphPathwayProgress setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public GraphPathwayProgress setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    @Override
    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public GraphPathwayProgress setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    @Override
    public UUID getStudentId() {
        return studentId;
    }

    public GraphPathwayProgress setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    @Override
    public UUID getAttemptId() {
        return attemptId;
    }

    public GraphPathwayProgress setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @Override
    public UUID getEvaluationId() {
        return evaluationId;
    }

    public GraphPathwayProgress setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    @Override
    public Completion getCompletion() {
        return completion;
    }

    public GraphPathwayProgress setCompletion(Completion completion) {
        this.completion = completion;
        return this;
    }

    public UUID getCurrentWalkableId() {
        return currentWalkableId;
    }

    public GraphPathwayProgress setCurrentWalkableId(UUID currentWalkableId) {
        this.currentWalkableId = currentWalkableId;
        return this;
    }

    public CoursewareElementType getCurrentWalkableType() {
        return currentWalkableType;
    }

    public GraphPathwayProgress setCurrentWalkableType(CoursewareElementType currentWalkableType) {
        this.currentWalkableType = currentWalkableType;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionValues() {
        return childWalkableCompletionValues;
    }

    public GraphPathwayProgress setChildWalkableCompletionValues(Map<UUID, Float> childWalkableCompletionValues) {
        this.childWalkableCompletionValues = childWalkableCompletionValues;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionConfidences() {
        return childWalkableCompletionConfidences;
    }

    public GraphPathwayProgress setChildWalkableCompletionConfidences(Map<UUID, Float> childWalkableCompletionConfidences) {
        this.childWalkableCompletionConfidences = childWalkableCompletionConfidences;
        return this;
    }

    public List<UUID> getCompletedWalkables() {
        return completedWalkables;
    }

    public GraphPathwayProgress setCompletedWalkables(List<UUID> completedWalkables) {
        this.completedWalkables = completedWalkables;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphPathwayProgress that = (GraphPathwayProgress) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                coursewareElementType == that.coursewareElementType &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(completion, that.completion) &&
                Objects.equals(currentWalkableId, that.currentWalkableId) &&
                currentWalkableType == that.currentWalkableType &&
                Objects.equals(childWalkableCompletionValues, that.childWalkableCompletionValues) &&
                Objects.equals(childWalkableCompletionConfidences, that.childWalkableCompletionConfidences) &&
                Objects.equals(completedWalkables, that.completedWalkables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                attemptId, evaluationId, completion, currentWalkableId, currentWalkableType, childWalkableCompletionValues,
                childWalkableCompletionConfidences, completedWalkables);
    }

    @Override
    public String toString() {
        return "GraphPathwayProgress{" +
                "id=" + id +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", coursewareElementId=" + coursewareElementId +
                ", coursewareElementType=" + coursewareElementType +
                ", studentId=" + studentId +
                ", attemptId=" + attemptId +
                ", evaluationId=" + evaluationId +
                ", completion=" + completion +
                ", currentWalkableId=" + currentWalkableId +
                ", currentWalkableType=" + currentWalkableType +
                ", childWalkableCompletionValues=" + childWalkableCompletionValues +
                ", childWalkableCompletionConfidences=" + childWalkableCompletionConfidences +
                ", completedWalkables=" + completedWalkables +
                '}';
    }
}
