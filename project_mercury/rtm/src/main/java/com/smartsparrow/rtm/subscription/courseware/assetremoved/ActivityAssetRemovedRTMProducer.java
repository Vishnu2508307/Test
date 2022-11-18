package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for an activity removed asset event
 */
public class ActivityAssetRemovedRTMProducer extends AbstractProducer<ActivityAssetRemovedRTMConsumable> {

    private ActivityAssetRemovedRTMConsumable activityAssetRemovedRTMConsumable;

    @Inject
    public ActivityAssetRemovedRTMProducer() {
    }

    public ActivityAssetRemovedRTMProducer buildActivityAssetRemovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                                  UUID rootElementId,
                                                                                  UUID activityId,
                                                                                  UUID parentPathwayId) {
        this.activityAssetRemovedRTMConsumable = new ActivityAssetRemovedRTMConsumable(rtmClientContext,
                                                                                       new ActivityCreatedBroadcastMessage(
                                                                                               rootElementId,
                                                                                               activityId,
                                                                                               parentPathwayId));
        return this;
    }

    @Override
    public ActivityAssetRemovedRTMConsumable getEventConsumable() {
        return activityAssetRemovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityAssetRemovedRTMProducer that = (ActivityAssetRemovedRTMProducer) o;
        return Objects.equals(activityAssetRemovedRTMConsumable, that.activityAssetRemovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityAssetRemovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityAssetRemovedRTMProducer{" +
                "activityAssetRemovedRTMConsumable=" + activityAssetRemovedRTMConsumable +
                '}';
    }
}
