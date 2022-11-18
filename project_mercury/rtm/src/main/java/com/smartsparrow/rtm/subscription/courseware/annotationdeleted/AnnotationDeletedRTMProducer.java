package com.smartsparrow.rtm.subscription.courseware.annotationdeleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly deleted annotation
 */
public class AnnotationDeletedRTMProducer extends AbstractProducer<AnnotationDeletedRTMConsumable> {

    private AnnotationDeletedRTMConsumable annotationDeletedRTMConsumable;

    @Inject
    public AnnotationDeletedRTMProducer() {
    }

    public AnnotationDeletedRTMProducer buildAnnotationDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                            UUID activityId,
                                                                            UUID elementId,
                                                                            CoursewareElementType elementType,
                                                                            UUID annotationId) {
        this.annotationDeletedRTMConsumable = new AnnotationDeletedRTMConsumable(rtmClientContext,
                                                                                 new AnnotationBroadcastMessage(
                                                                                         activityId,
                                                                                         elementId,
                                                                                         elementType,
                                                                                         annotationId));
        return this;
    }

    @Override
    public AnnotationDeletedRTMConsumable getEventConsumable() {
        return annotationDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationDeletedRTMProducer that = (AnnotationDeletedRTMProducer) o;
        return Objects.equals(annotationDeletedRTMConsumable, that.annotationDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AnnotationDeletedRTMProducer{" +
                "annotationDeletedRTMConsumable=" + annotationDeletedRTMConsumable +
                '}';
    }
}
