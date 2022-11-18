package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.AssetRemovedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a removed asset
 */
public class AssetRemovedRTMProducer extends AbstractProducer<AssetRemovedRTMConsumable> {

    private AssetRemovedRTMConsumable assetRemovedRTMConsumable;

    @Inject
    public AssetRemovedRTMProducer() {
    }

    public AssetRemovedRTMProducer buildAssetRemovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                  UUID activityId,
                                                                  UUID elementId,
                                                                  CoursewareElementType elementType) {
        this.assetRemovedRTMConsumable = new AssetRemovedRTMConsumable(rtmClientContext,
                                                                       new AssetRemovedBroadcastMessage(
                                                                               activityId,
                                                                               elementId,
                                                                               elementType));
        return this;
    }

    @Override
    public AssetRemovedRTMConsumable getEventConsumable() {
        return assetRemovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetRemovedRTMProducer that = (AssetRemovedRTMProducer) o;
        return Objects.equals(assetRemovedRTMConsumable, that.assetRemovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetRemovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AssetRemovedRTMProducer{" +
                "assetRemovedRTMConsumable=" + assetRemovedRTMConsumable +
                '}';
    }
}
