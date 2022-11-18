package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class AnnotationBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 6691414580645745332L;
    private final UUID annotationId;

    public AnnotationBroadcastMessage(final UUID activityId,
                                      final UUID elementId,
                                      final CoursewareElementType type,
                                      final UUID annotationId) {
        super(activityId, elementId, type);
        this.annotationId = annotationId;
    }

    public UUID getAnnotationId() {
        return annotationId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnnotationBroadcastMessage that = (AnnotationBroadcastMessage) o;
        return Objects.equals(annotationId, that.annotationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), annotationId);
    }

    @Override
    public String toString() {
        return "AnnotationBroadcastMessage{" +
                "annotationId=" + annotationId +
                "} " + super.toString();
    }
}
