package com.smartsparrow.rtm.subscription.courseware.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.PathwayCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create activity pathway
 */
public class PathwayCreatedRTMProducer extends AbstractProducer<PathwayCreatedRTMConsumable> {

    private PathwayCreatedRTMConsumable pathwayCreatedRTMConsumable;

    @Inject
    public PathwayCreatedRTMProducer() {
    }

    public PathwayCreatedRTMProducer buildPathwayCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                      UUID activityId,
                                                                      UUID pathwayId) {
        this.pathwayCreatedRTMConsumable = new PathwayCreatedRTMConsumable(rtmClientContext,
                                                                           new PathwayCreatedBroadcastMessage(activityId,
                                                                                                              pathwayId));
        return this;
    }

    @Override
    public PathwayCreatedRTMConsumable getEventConsumable() {
        return pathwayCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayCreatedRTMProducer that = (PathwayCreatedRTMProducer) o;
        return Objects.equals(pathwayCreatedRTMConsumable, that.pathwayCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "PathwayCreatedRTMProducer{" +
                "pathwayCreatedRTMConsumable=" + pathwayCreatedRTMConsumable +
                '}';
    }
}
