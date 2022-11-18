package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class DocumentItemTag {

    private UUID documentItemId;
    private UUID documentId;
    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public DocumentItemTag setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentItemTag setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public DocumentItemTag setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public DocumentItemTag setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemTag that = (DocumentItemTag) o;
        return Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemId, documentId, elementId, elementType);
    }

    @Override
    public String toString() {
        return "DocumentItemTag{" +
                "documentItemId=" + documentItemId +
                ", documentId=" + documentId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
