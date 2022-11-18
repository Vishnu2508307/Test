package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a config changed pathway
 */
public class PathwayConfigChangeRTMProducer extends AbstractProducer<PathwayConfigChangeRTMConsumable> {

    private PathwayConfigChangeRTMConsumable pathwayConfigChangeRTMConsumable;

    @Inject
    public PathwayConfigChangeRTMProducer() {
    }

    public PathwayConfigChangeRTMProducer buildPathwayConfigChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                                UUID activityId,
                                                                                UUID pathwayId,
                                                                                String config) {
        this.pathwayConfigChangeRTMConsumable = new PathwayConfigChangeRTMConsumable(rtmClientContext,
                                                                                     new ConfigChangeBroadcastMessage(
                                                                                             activityId,
                                                                                             pathwayId,
                                                                                             PATHWAY,
                                                                                             config));
        return this;
    }

    @Override
    public PathwayConfigChangeRTMConsumable getEventConsumable() {
        return pathwayConfigChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayConfigChangeRTMProducer that = (PathwayConfigChangeRTMProducer) o;
        return Objects.equals(pathwayConfigChangeRTMConsumable, that.pathwayConfigChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayConfigChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "PathwayConfigChangeRTMProducer{" +
                "pathwayConfigChangeRTMConsumable=" + pathwayConfigChangeRTMConsumable +
                '}';
    }
}
