package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for an activity removed assets event
 */
public class ActivityAssetsRemovedRTMProducer extends AbstractProducer<ActivityAssetsRemovedRTMConsumable> {

    private ActivityAssetsRemovedRTMConsumable activityAssetsRemovedRTMConsumable;

    @Inject
    public ActivityAssetsRemovedRTMProducer() {
    }

    public ActivityAssetsRemovedRTMProducer buildActivityAssetsRemovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                                    UUID rootElementId,
                                                                                    UUID activityId,
                                                                                    UUID parentPathwayId) {
        this.activityAssetsRemovedRTMConsumable = new ActivityAssetsRemovedRTMConsumable(rtmClientContext,
                                                                                         new ActivityCreatedBroadcastMessage(
                                                                                                 rootElementId,
                                                                                                 activityId,
                                                                                                 parentPathwayId));
        return this;
    }

    @Override
    public ActivityAssetsRemovedRTMConsumable getEventConsumable() {
        return activityAssetsRemovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityAssetsRemovedRTMProducer that = (ActivityAssetsRemovedRTMProducer) o;
        return Objects.equals(activityAssetsRemovedRTMConsumable, that.activityAssetsRemovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityAssetsRemovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityAssetsRemovedRTMProducer{" +
                "activityAssetsRemovedRTMConsumable=" + activityAssetsRemovedRTMConsumable +
                '}';
    }
}
