package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;

/**
 * This RTM producer produces an RTM event for an optimized asset event
 */
public class AssetOptimizedRTMProducer extends AbstractProducer<AssetOptimizedRTMConsumable> {

    private AssetOptimizedRTMConsumable assetOptimizedRTMConsumable;

    @Inject
    public AssetOptimizedRTMProducer() {
    }

    public AssetOptimizedRTMProducer buildAssetOptimizedRTMConsumable(UUID activityId,
                                                                      UUID elementId,
                                                                      Object elementType,
                                                                      UUID assetId,
                                                                      String assetUrl) {
        this.assetOptimizedRTMConsumable = new AssetOptimizedRTMConsumable(
                new AssetOptimizedBroadcastMessage(
                        activityId,
                        elementId,
                        elementType,
                        assetId,
                        assetUrl));
        return this;
    }

    @Override
    public AssetOptimizedRTMConsumable getEventConsumable() {
        return assetOptimizedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetOptimizedRTMProducer that = (AssetOptimizedRTMProducer) o;
        return Objects.equals(assetOptimizedRTMConsumable, that.assetOptimizedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetOptimizedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AssetOptimizedRTMProducer{" +
                "assetOptimizedRTMConsumable=" + assetOptimizedRTMConsumable +
                '}';
    }
}
