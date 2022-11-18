package com.smartsparrow.annotation.service;

import java.util.Objects;
import java.util.UUID;

/**
 * Object to store an Annotation read for a user
 */
public class CoursewareAnnotationReadByUser {

    private UUID rootElementId;
    private UUID elementId;
    private UUID annotationId;
    private UUID userId;

    public CoursewareAnnotationReadByUser() {
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public CoursewareAnnotationReadByUser setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareAnnotationReadByUser setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getAnnotationId() {
        return annotationId;
    }

    public CoursewareAnnotationReadByUser setAnnotationId(UUID annotationId) {
        this.annotationId = annotationId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public CoursewareAnnotationReadByUser setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareAnnotationReadByUser that = (CoursewareAnnotationReadByUser) o;
        return Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(annotationId, that.annotationId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, elementId, annotationId, userId);
    }

    @Override
    public String toString() {
        return "CoursewareAnnotationReadByUser{" +
                "rootElementId=" + rootElementId +
                ", elementId=" + elementId +
                ", annotationId=" + annotationId +
                ", userId=" + userId +
                '}';
    }
}
