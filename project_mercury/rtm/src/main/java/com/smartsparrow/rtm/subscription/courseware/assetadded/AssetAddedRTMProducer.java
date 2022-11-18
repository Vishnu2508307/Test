package com.smartsparrow.rtm.subscription.courseware.assetadded;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.AssetAddedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly added asset
 */
public class AssetAddedRTMProducer extends AbstractProducer<AssetAddedRTMConsumable> {

    private AssetAddedRTMConsumable assetAddedRTMConsumable;

    @Inject
    public AssetAddedRTMProducer() {
    }

    public AssetAddedRTMProducer buildAssetAddedRTMConsumable(RTMClientContext rtmClientContext,
                                                              UUID activityId,
                                                              UUID elementId,
                                                              CoursewareElementType elementType) {
        this.assetAddedRTMConsumable = new AssetAddedRTMConsumable(rtmClientContext,
                                                                   new AssetAddedBroadcastMessage(
                                                                           activityId,
                                                                           elementId,
                                                                           elementType));
        return this;
    }

    @Override
    public AssetAddedRTMConsumable getEventConsumable() {
        return assetAddedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetAddedRTMProducer that = (AssetAddedRTMProducer) o;
        return Objects.equals(assetAddedRTMConsumable, that.assetAddedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetAddedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AssetAddedRTMProducer{" +
                "assetAddedRTMConsumable=" + assetAddedRTMConsumable +
                '}';
    }
}
