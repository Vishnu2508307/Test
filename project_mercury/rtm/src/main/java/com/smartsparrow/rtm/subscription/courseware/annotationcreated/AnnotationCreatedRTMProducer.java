package com.smartsparrow.rtm.subscription.courseware.annotationcreated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create annotation
 */
public class AnnotationCreatedRTMProducer extends AbstractProducer<AnnotationCreatedRTMConsumable> {

    private AnnotationCreatedRTMConsumable annotationCreatedRTMConsumable;

    @Inject
    public AnnotationCreatedRTMProducer() {
    }

    public AnnotationCreatedRTMProducer buildAnnotationCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                            UUID activityId,
                                                                            UUID elementId,
                                                                            CoursewareElementType elementType,
                                                                            UUID annotationId) {
        this.annotationCreatedRTMConsumable = new AnnotationCreatedRTMConsumable(rtmClientContext,
                                                                                 new AnnotationBroadcastMessage(
                                                                                         activityId,
                                                                                         elementId,
                                                                                         elementType,
                                                                                         annotationId));
        return this;
    }

    @Override
    public AnnotationCreatedRTMConsumable getEventConsumable() {
        return annotationCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationCreatedRTMProducer that = (AnnotationCreatedRTMProducer) o;
        return Objects.equals(annotationCreatedRTMConsumable, that.annotationCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AnnotationCreatedRTMProducer{" +
                "annotationCreatedRTMConsumable=" + annotationCreatedRTMConsumable +
                '}';
    }
}
