package com.smartsparrow.rtm.subscription.courseware.duplicated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly duplicated activity
 */
public class ActivityDuplicatedRTMProducer extends AbstractProducer<ActivityDuplicatedRTMConsumable> {

    private ActivityDuplicatedRTMConsumable activityDuplicatedRTMConsumable;

    @Inject
    public ActivityDuplicatedRTMProducer() {
    }

    public ActivityDuplicatedRTMProducer buildActivityDuplicatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                              UUID rootElementId,
                                                                              UUID activityId,
                                                                              UUID parentPathwayId) {
        this.activityDuplicatedRTMConsumable = new ActivityDuplicatedRTMConsumable(rtmClientContext,
                                                                                   new ActivityCreatedBroadcastMessage(
                                                                                           rootElementId,
                                                                                           activityId,
                                                                                           parentPathwayId));
        return this;
    }

    @Override
    public ActivityDuplicatedRTMConsumable getEventConsumable() {
        return activityDuplicatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityDuplicatedRTMProducer that = (ActivityDuplicatedRTMProducer) o;
        return Objects.equals(activityDuplicatedRTMConsumable, that.activityDuplicatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityDuplicatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityDuplicatedRTMProducer{" +
                "activityDuplicatedRTMConsumable=" + activityDuplicatedRTMConsumable +
                '}';
    }
}
