package com.smartsparrow.rtm.subscription.courseware.moved;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ElementMovedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a moved interactive
 */
public class InteractiveMovedRTMProducer extends AbstractProducer<InteractiveMovedRTMConsumable> {

    private InteractiveMovedRTMConsumable interactiveMovedRTMConsumable;

    @Inject
    public InteractiveMovedRTMProducer() {
    }

    public InteractiveMovedRTMProducer buildInteractiveMovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                          UUID activityId,
                                                                          UUID interactiveId,
                                                                          UUID fromPathwayId,
                                                                          UUID toPathwayId) {
        this.interactiveMovedRTMConsumable = new InteractiveMovedRTMConsumable(rtmClientContext,
                                                                               new ElementMovedBroadcastMessage(
                                                                                       activityId,
                                                                                       interactiveId,
                                                                                       INTERACTIVE,
                                                                                       fromPathwayId,
                                                                                       toPathwayId));
        return this;
    }

    @Override
    public InteractiveMovedRTMConsumable getEventConsumable() {
        return interactiveMovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveMovedRTMProducer that = (InteractiveMovedRTMProducer) o;
        return Objects.equals(interactiveMovedRTMConsumable, that.interactiveMovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveMovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "InteractiveMovedRTMProducer{" +
                "interactiveMovedRTMConsumable=" + interactiveMovedRTMConsumable +
                '}';
    }
}
