package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class LearnerDocumentItemTag {

    private UUID documentItemId;
    private UUID documentId;
    private UUID elementId;
    private CoursewareElementType elementType;

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public LearnerDocumentItemTag setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public LearnerDocumentItemTag setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public LearnerDocumentItemTag setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public LearnerDocumentItemTag setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerDocumentItemTag setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerDocumentItemTag setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerDocumentItemTag that = (LearnerDocumentItemTag) o;
        return Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemId, documentId, elementId, elementType, deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerDocumentItemTag{" +
                "documentItemId=" + documentItemId +
                ", documentId=" + documentId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                '}';
    }
}
