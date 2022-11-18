package com.smartsparrow.rtm.subscription.courseware.pathwayreordered;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.PathwayReOrderedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a pathway reordered
 */
public class PathwayReOrderedRTMProducer extends AbstractProducer<PathwayReOrderedRTMConsumable> {

    private PathwayReOrderedRTMConsumable pathwayReOrderedRTMConsumable;

    @Inject
    public PathwayReOrderedRTMProducer() {
    }

    public PathwayReOrderedRTMProducer buildPathwayReOrderedRTMConsumable(RTMClientContext rtmClientContext,
                                                                          UUID activityId,
                                                                          UUID pathwayId,
                                                                          List<WalkableChild> walkableIds) {
        this.pathwayReOrderedRTMConsumable = new PathwayReOrderedRTMConsumable(rtmClientContext,
                                                                               new PathwayReOrderedBroadcastMessage(
                                                                                       activityId,
                                                                                       pathwayId,
                                                                                       walkableIds));
        return this;
    }

    @Override
    public PathwayReOrderedRTMConsumable getEventConsumable() {
        return pathwayReOrderedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayReOrderedRTMProducer that = (PathwayReOrderedRTMProducer) o;
        return Objects.equals(pathwayReOrderedRTMConsumable, that.pathwayReOrderedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayReOrderedRTMConsumable);
    }

    @Override
    public String toString() {
        return "PathwayReOrderedRTMProducer{" +
                "pathwayReOrderedRTMConsumable=" + pathwayReOrderedRTMConsumable +
                '}';
    }
}
