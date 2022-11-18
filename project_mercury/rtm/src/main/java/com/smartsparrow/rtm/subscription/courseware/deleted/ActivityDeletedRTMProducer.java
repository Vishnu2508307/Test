package com.smartsparrow.rtm.subscription.courseware.deleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a deleted activity
 */
public class ActivityDeletedRTMProducer extends AbstractProducer<ActivityDeletedRTMConsumable> {

    private ActivityDeletedRTMConsumable activityDeletedRTMConsumable;

    @Inject
    public ActivityDeletedRTMProducer() {
    }

    public ActivityDeletedRTMProducer buildActivityDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                        UUID rootElementId,
                                                                        UUID activityId,
                                                                        UUID parentPathwayId) {
        this.activityDeletedRTMConsumable = new ActivityDeletedRTMConsumable(rtmClientContext,
                                                                             new ActivityCreatedBroadcastMessage(
                                                                                     rootElementId,
                                                                                     activityId,
                                                                                     parentPathwayId));
        return this;
    }

    @Override
    public ActivityDeletedRTMConsumable getEventConsumable() {
        return activityDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityDeletedRTMProducer that = (ActivityDeletedRTMProducer) o;
        return Objects.equals(activityDeletedRTMConsumable, that.activityDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityDeletedRTMProducer{" +
                "activityDeletedRTMConsumable=" + activityDeletedRTMConsumable +
                '}';
    }
}
