package com.smartsparrow.learner.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerBKTPathway;

public class BKTPathwayProgress implements Progress {

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
    private Double pLnMinusGivenActual;
    private Double pLn;
    private Double pCorrect;

    //
    private Map<UUID, Float> childWalkableCompletionValues = new HashMap<>();
    private Map<UUID, Float> childWalkableCompletionConfidences = new HashMap<>();
    private List<UUID> completedWalkables = new ArrayList<>();

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

    public BKTPathwayProgress setId(final UUID id) {
        this.id = id;
        return this;
    }

    public BKTPathwayProgress setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public BKTPathwayProgress setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public BKTPathwayProgress setCoursewareElementId(final UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public BKTPathwayProgress setCoursewareElementType(final CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    public BKTPathwayProgress setStudentId(final UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public BKTPathwayProgress setAttemptId(final UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public BKTPathwayProgress setEvaluationId(final UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    public BKTPathwayProgress setCompletion(final Completion completion) {
        this.completion = completion;
        return this;
    }

    /**
     * When a student attempts a walkable with no success, the next time they access the bkt pathway they should be
     * presented with the walkable in progress. This method is used to determine which walkable should be shown to
     * the student when the {@link LearnerBKTPathway#supplyRelevantWalkables(UUID)} is invoked.
     *
     * @return the in progress element for this pathway
     */
    @Nullable
    public UUID getInProgressElementId() {
        return inProgressElementId;
    }

    public BKTPathwayProgress setInProgressElementId(final UUID inProgressElementId) {
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

    public BKTPathwayProgress setInProgressElementType(final CoursewareElementType inProgressElementType) {
        this.inProgressElementType = inProgressElementType;
        return this;
    }

    public Double getpLnMinusGivenActual() {
        return pLnMinusGivenActual;
    }

    public BKTPathwayProgress setpLnMinusGivenActual(final Double pLnMinusGivenActual) {
        this.pLnMinusGivenActual = pLnMinusGivenActual;
        return this;
    }

    public Double getpLn() {
        return pLn;
    }

    public BKTPathwayProgress setpLn(final Double pLn) {
        this.pLn = pLn;
        return this;
    }

    public Double getpCorrect() {
        return pCorrect;
    }

    public BKTPathwayProgress setpCorrect(final Double pCorrect) {
        this.pCorrect = pCorrect;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionValues() {
        return childWalkableCompletionValues;
    }

    public BKTPathwayProgress setChildWalkableCompletionValues(final Map<UUID, Float> childWalkableCompletionValues) {
        this.childWalkableCompletionValues = childWalkableCompletionValues;
        return this;
    }

    public Map<UUID, Float> getChildWalkableCompletionConfidences() {
        return childWalkableCompletionConfidences;
    }

    public BKTPathwayProgress setChildWalkableCompletionConfidences(final Map<UUID, Float> childWalkableCompletionConfidences) {
        this.childWalkableCompletionConfidences = childWalkableCompletionConfidences;
        return this;
    }

    public List<UUID> getCompletedWalkables() {
        return completedWalkables;
    }

    public BKTPathwayProgress setCompletedWalkables(final List<UUID> completedWalkables) {
        this.completedWalkables = completedWalkables;
        return this;
    }

    /**
     * @return true when the completion is not null and the completion value is equal to 1
     */
    public boolean isCompleted() {
        return completion != null && completion.getValue() == 1f;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BKTPathwayProgress that = (BKTPathwayProgress) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                coursewareElementType == that.coursewareElementType &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(completion, that.completion) &&
                Objects.equals(inProgressElementId, that.inProgressElementId) &&
                inProgressElementType == that.inProgressElementType &&
                Objects.equals(pLnMinusGivenActual, that.pLnMinusGivenActual) &&
                Objects.equals(pLn, that.pLn) &&
                Objects.equals(pCorrect, that.pCorrect) &&
                Objects.equals(childWalkableCompletionValues, that.childWalkableCompletionValues) &&
                Objects.equals(childWalkableCompletionConfidences, that.childWalkableCompletionConfidences) &&
                Objects.equals(completedWalkables, that.completedWalkables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deploymentId, changeId, coursewareElementId, coursewareElementType, studentId,
                attemptId, evaluationId, completion, inProgressElementId, inProgressElementType, pLnMinusGivenActual,
                pLn, pCorrect, childWalkableCompletionValues, childWalkableCompletionConfidences, completedWalkables);
    }

    @Override
    public String toString() {
        return "BKTPathwayProgress{" +
                "id=" + id +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", coursewareElementId=" + coursewareElementId +
                ", coursewareElementType=" + coursewareElementType +
                ", studentId=" + studentId +
                ", attemptId=" + attemptId +
                ", evaluationId=" + evaluationId +
                ", completion=" + completion +
                ", inProgressElementId=" + inProgressElementId +
                ", inProgressElementType=" + inProgressElementType +
                ", pLnMinusGivenActual=" + pLnMinusGivenActual +
                ", pLn=" + pLn +
                ", pCorrect=" + pCorrect +
                ", childWalkableCompletionValues=" + childWalkableCompletionValues +
                ", childWalkableCompletionConfidences=" + childWalkableCompletionConfidences +
                ", completedWalkables=" + completedWalkables +
                '}';
    }
}
