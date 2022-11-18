package com.smartsparrow.rtm.subscription.courseware.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.InteractiveCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create interactive
 */
public class InteractiveCreatedRTMProducer extends AbstractProducer<InteractiveCreatedRTMConsumable> {

    private InteractiveCreatedRTMConsumable interactiveCreatedRTMConsumable;

    @Inject
    public InteractiveCreatedRTMProducer() {
    }

    public InteractiveCreatedRTMProducer buildInteractiveCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                              UUID activityId,
                                                                              UUID interactiveId,
                                                                              UUID parentPathwayId) {
        this.interactiveCreatedRTMConsumable = new InteractiveCreatedRTMConsumable(rtmClientContext,
                                                                                   new InteractiveCreatedBroadcastMessage(
                                                                                           activityId,
                                                                                           interactiveId,
                                                                                           parentPathwayId));
        return this;
    }

    @Override
    public InteractiveCreatedRTMConsumable getEventConsumable() {
        return interactiveCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveCreatedRTMProducer that = (InteractiveCreatedRTMProducer) o;
        return Objects.equals(interactiveCreatedRTMConsumable, that.interactiveCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "InteractiveCreatedRTMProducer{" +
                "interactiveCreatedRTMConsumable=" + interactiveCreatedRTMConsumable +
                '}';
    }
}
