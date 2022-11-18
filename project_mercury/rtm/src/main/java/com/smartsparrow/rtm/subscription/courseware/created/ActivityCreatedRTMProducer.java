package com.smartsparrow.rtm.subscription.courseware.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create activity
 */
public class ActivityCreatedRTMProducer extends AbstractProducer<ActivityCreatedRTMConsumable> {

    private ActivityCreatedRTMConsumable activityCreatedRTMConsumable;

    @Inject
    public ActivityCreatedRTMProducer() {
    }

    public ActivityCreatedRTMProducer buildActivityCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                        UUID rootElementId,
                                                                        UUID activityId,
                                                                        UUID parentPathwayId) {
        this.activityCreatedRTMConsumable = new ActivityCreatedRTMConsumable(rtmClientContext,
                                                                             new ActivityCreatedBroadcastMessage(
                                                                                     rootElementId,
                                                                                     activityId,
                                                                                     parentPathwayId));
        return this;
    }

    @Override
    public ActivityCreatedRTMConsumable getEventConsumable() {
        return activityCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityCreatedRTMProducer that = (ActivityCreatedRTMProducer) o;
        return Objects.equals(activityCreatedRTMConsumable, that.activityCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityCreatedRTMProducer{" +
                "activityCreatedRTMConsumable=" + activityCreatedRTMConsumable +
                '}';
    }
}
