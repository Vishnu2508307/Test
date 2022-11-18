package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a deleted activity pathway
 */
public class PathwayDeletedRTMProducer extends AbstractProducer<PathwayDeletedRTMConsumable> {

    private PathwayDeletedRTMConsumable pathwayDeletedRTMConsumable;

    @Inject
    public PathwayDeletedRTMProducer() {
    }

    public PathwayDeletedRTMProducer buildPathwayDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                      UUID rootElementId,
                                                                      UUID pathwayId) {
        this.pathwayDeletedRTMConsumable = new PathwayDeletedRTMConsumable(rtmClientContext,
                                                                           new ActivityBroadcastMessage(rootElementId,
                                                                                                        pathwayId,
                                                                                                        PATHWAY));
        return this;
    }

    @Override
    public PathwayDeletedRTMConsumable getEventConsumable() {
        return pathwayDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayDeletedRTMProducer that = (PathwayDeletedRTMProducer) o;
        return Objects.equals(pathwayDeletedRTMConsumable, that.pathwayDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "PathwayDeletedRTMProducer{" +
                "pathwayDeletedRTMConsumable=" + pathwayDeletedRTMConsumable +
                '}';
    }
}
