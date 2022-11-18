package com.smartsparrow.rtm.subscription.courseware.deleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.InteractiveCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a deleted activity interactive
 */
public class InteractiveDeletedRTMProducer extends AbstractProducer<InteractiveDeletedRTMConsumable> {

    private InteractiveDeletedRTMConsumable interactiveDeletedRTMConsumable;

    @Inject
    public InteractiveDeletedRTMProducer() {
    }

    public InteractiveDeletedRTMProducer buildInteractiveDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                              UUID activityId,
                                                                              UUID interactiveId,
                                                                              UUID parentPathwayId) {
        this.interactiveDeletedRTMConsumable = new InteractiveDeletedRTMConsumable(rtmClientContext,
                                                                                   new InteractiveCreatedBroadcastMessage(
                                                                                           activityId,
                                                                                           interactiveId,
                                                                                           parentPathwayId));
        return this;
    }

    @Override
    public InteractiveDeletedRTMConsumable getEventConsumable() {
        return interactiveDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveDeletedRTMProducer that = (InteractiveDeletedRTMProducer) o;
        return Objects.equals(interactiveDeletedRTMConsumable, that.interactiveDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "InteractiveDeletedRTMProducer{" +
                "interactiveDeletedRTMConsumable=" + interactiveDeletedRTMConsumable +
                '}';
    }
}
