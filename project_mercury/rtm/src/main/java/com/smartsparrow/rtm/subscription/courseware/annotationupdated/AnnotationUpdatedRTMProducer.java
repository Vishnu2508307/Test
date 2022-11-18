package com.smartsparrow.rtm.subscription.courseware.annotationupdated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly updated activity annotation
 */
public class AnnotationUpdatedRTMProducer extends AbstractProducer<AnnotationUpdatedRTMConsumable> {

    private AnnotationUpdatedRTMConsumable annotationUpdatedRTMConsumable;

    @Inject
    public AnnotationUpdatedRTMProducer() {
    }

    public AnnotationUpdatedRTMProducer buildAnnotationUpdatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                            UUID activityId,
                                                                            UUID elementId,
                                                                            CoursewareElementType elementType,
                                                                            UUID annotationId) {
        this.annotationUpdatedRTMConsumable = new AnnotationUpdatedRTMConsumable(rtmClientContext,
                                                                                 new AnnotationBroadcastMessage(
                                                                                         activityId,
                                                                                         elementId,
                                                                                         elementType,
                                                                                         annotationId));
        return this;
    }

    @Override
    public AnnotationUpdatedRTMConsumable getEventConsumable() {
        return annotationUpdatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationUpdatedRTMProducer that = (AnnotationUpdatedRTMProducer) o;
        return Objects.equals(annotationUpdatedRTMConsumable, that.annotationUpdatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationUpdatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AnnotationUpdatedRTMProducer{" +
                "annotationUpdatedRTMConsumable=" + annotationUpdatedRTMConsumable +
                '}';
    }
}
