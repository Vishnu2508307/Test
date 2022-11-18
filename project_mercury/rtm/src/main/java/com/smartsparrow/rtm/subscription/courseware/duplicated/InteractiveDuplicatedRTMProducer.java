package com.smartsparrow.rtm.subscription.courseware.duplicated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.InteractiveCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly duplicated activity interactive
 */
public class InteractiveDuplicatedRTMProducer extends AbstractProducer<InteractiveDuplicatedRTMConsumable> {

    private InteractiveDuplicatedRTMConsumable interactiveDuplicatedRTMConsumable;

    @Inject
    public InteractiveDuplicatedRTMProducer() {
    }

    public InteractiveDuplicatedRTMProducer buildInteractiveDuplicatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                                    UUID activityId,
                                                                                    UUID interactiveId,
                                                                                    UUID parentPathwayId) {
        this.interactiveDuplicatedRTMConsumable = new InteractiveDuplicatedRTMConsumable(rtmClientContext,
                                                                                         new InteractiveCreatedBroadcastMessage(
                                                                                                 activityId,
                                                                                                 interactiveId,
                                                                                                 parentPathwayId));
        return this;
    }

    @Override
    public InteractiveDuplicatedRTMConsumable getEventConsumable() {
        return interactiveDuplicatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveDuplicatedRTMProducer that = (InteractiveDuplicatedRTMProducer) o;
        return Objects.equals(interactiveDuplicatedRTMConsumable, that.interactiveDuplicatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveDuplicatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "InteractiveDuplicatedRTMProducer{" +
                "interactiveDuplicatedRTMConsumable=" + interactiveDuplicatedRTMConsumable +
                '}';
    }
}
