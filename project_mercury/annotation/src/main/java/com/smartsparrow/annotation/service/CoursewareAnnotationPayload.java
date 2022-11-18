package com.smartsparrow.annotation.service;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoursewareAnnotationPayload extends CoursewareAnnotation {

    private Boolean read;

    public CoursewareAnnotationPayload(final CoursewareAnnotation annotation) {
        id = annotation.getId();
        version = annotation.getVersion();
        motivation = annotation.getMotivation();
        creatorAccountId = annotation.getCreatorAccountId();
        elementId = annotation.getElementId();
        bodyJson = annotation.getBodyJson();
        targetJson = annotation.getTargetJson();
        rootElementId = annotation.getRootElementId();
        resolved = annotation.getResolved();
    }

    public Boolean getRead() {
        return read;
    }

    public CoursewareAnnotationPayload setRead(Boolean read) {
        this.read = read;
        return this;
    }

    /**
     * Helper method to create {@link CoursewareAnnotationPayload}
     * @param annotation annotation
     * @param readByUser read status, can be empty object if not read
     * @return annotation payload
     */
    public static CoursewareAnnotationPayload from(@Nonnull CoursewareAnnotation annotation,
                                                   CoursewareAnnotationReadByUser readByUser) {
        CoursewareAnnotationPayload payload = new CoursewareAnnotationPayload(annotation)
                .setRead(false);

        // if the read by user object is not null, set to true
        if (readByUser.getAnnotationId() != null) {
            payload.setRead(true);
        }
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CoursewareAnnotationPayload that = (CoursewareAnnotationPayload) o;
        return Objects.equals(read, that.read);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), read);
    }

    @Override
    public String toString() {
        return "CoursewareAnnotationPayload{"
                + "read=" + read
                + "} " + super.toString();
    }
}
