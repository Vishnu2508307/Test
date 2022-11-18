package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The competency met by a student")
public class CompetencyMetByStudent {

    private UUID studentId;
    private UUID documentId;
    private UUID documentItemId;
    private UUID metId;
    private Float value;
    private Float confidence;

    public UUID getStudentId() {
        return studentId;
    }

    public CompetencyMetByStudent setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public CompetencyMetByStudent setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public CompetencyMetByStudent setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    public UUID getMetId() {
        return metId;
    }

    public CompetencyMetByStudent setMetId(UUID metId) {
        this.metId = metId;
        return this;
    }

    public Float getValue() {
        return value;
    }

    public CompetencyMetByStudent setValue(Float value) {
        this.value = value;
        return this;
    }

    public Float getConfidence() {
        return confidence;
    }

    public CompetencyMetByStudent setConfidence(Float confidence) {
        this.confidence = confidence;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyMetByStudent that = (CompetencyMetByStudent) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(metId, that.metId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(confidence, that.confidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, documentId, documentItemId, metId, value, confidence);
    }

    @Override
    public String toString() {
        return "CompetencyMetByStudent{" +
                "studentId=" + studentId +
                ", documentId=" + documentId +
                ", documentItemId=" + documentItemId +
                ", metId=" + metId +
                ", value=" + value +
                ", confidence=" + confidence +
                '}';
    }
}
