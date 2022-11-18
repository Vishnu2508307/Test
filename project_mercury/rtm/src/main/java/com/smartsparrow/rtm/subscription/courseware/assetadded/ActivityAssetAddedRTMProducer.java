package com.smartsparrow.rtm.subscription.courseware.assetadded;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for an activity added asset event
 */
public class ActivityAssetAddedRTMProducer extends AbstractProducer<ActivityAssetAddedRTMConsumable> {

    private ActivityAssetAddedRTMConsumable activityAssetAddedRTMConsumable;

    @Inject
    public ActivityAssetAddedRTMProducer() {
    }

    public ActivityAssetAddedRTMProducer buildActivityAssetAddedRTMConsumable(RTMClientContext rtmClientContext,
                                                                              UUID rootElementId,
                                                                              UUID activityId,
                                                                              UUID parentPathwayId) {
        this.activityAssetAddedRTMConsumable = new ActivityAssetAddedRTMConsumable(rtmClientContext,
                                                                                   new ActivityCreatedBroadcastMessage(
                                                                                           rootElementId,
                                                                                           activityId,
                                                                                           parentPathwayId));
        return this;
    }

    @Override
    public ActivityAssetAddedRTMConsumable getEventConsumable() {
        return activityAssetAddedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityAssetAddedRTMProducer that = (ActivityAssetAddedRTMProducer) o;
        return Objects.equals(activityAssetAddedRTMConsumable, that.activityAssetAddedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityAssetAddedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityAssetAddedRTMProducer{" +
                "activityAssetAddedRTMConsumable=" + activityAssetAddedRTMConsumable +
                '}';
    }
}
