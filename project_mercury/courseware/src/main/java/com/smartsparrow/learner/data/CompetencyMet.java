package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The competency met full object")
public class CompetencyMet {

    private UUID id;
    private UUID studentId;
    private UUID deploymentId;
    private UUID changeId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType;
    private UUID evaluationId;
    private UUID documentId;
    private UUID documentVersionId;
    private UUID documentItemId;
    private UUID attemptId;
    private Float value;
    private Float confidence;

    public UUID getId() {
        return id;
    }

    public CompetencyMet setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public CompetencyMet setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public CompetencyMet setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getChangeId() {
        return changeId;
    }

    public CompetencyMet setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public CompetencyMet setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public CompetencyMet setCoursewareElementType(CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    public UUID getEvaluationId() {
        return evaluationId;
    }

    public CompetencyMet setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public CompetencyMet setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getDocumentVersionId() {
        return documentVersionId;
    }

    public CompetencyMet setDocumentVersionId(UUID documentVersionId) {
        this.documentVersionId = documentVersionId;
        return this;
    }

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public CompetencyMet setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public CompetencyMet setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public Float getValue() {
        return value;
    }

    public CompetencyMet setValue(Float value) {
        this.value = value;
        return this;
    }

    public Float getConfidence() {
        return confidence;
    }

    public CompetencyMet setConfidence(Float confidence) {
        this.confidence = confidence;
        return this;
    }

    @GraphQLQuery(name = "modifiedAt")
    @JsonProperty(value = "modifiedAt")
    public String getFormattedModifiedAt() {
        return DateFormat.asRFC1123(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyMet that = (CompetencyMet) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                coursewareElementType == that.coursewareElementType &&
                Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(documentVersionId, that.documentVersionId) &&
                Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(confidence, that.confidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, deploymentId, changeId, coursewareElementId, coursewareElementType,
                evaluationId, documentId, documentVersionId, documentItemId, attemptId, value, confidence);
    }

    @Override
    public String toString() {
        return "CompetencyMet{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", coursewareElementId=" + coursewareElementId +
                ", coursewareElementType=" + coursewareElementType +
                ", evaluationId=" + evaluationId +
                ", documentId=" + documentId +
                ", documentVersionId=" + documentVersionId +
                ", documentItemId=" + documentItemId +
                ", attemptId=" + attemptId +
                ", value=" + value +
                ", confidence=" + confidence +
                '}';
    }
}
