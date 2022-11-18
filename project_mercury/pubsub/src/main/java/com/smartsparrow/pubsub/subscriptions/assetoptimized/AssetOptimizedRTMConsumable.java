package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes an asset optimized event
 */
public class AssetOptimizedRTMConsumable extends AbstractConsumable<AssetOptimizedBroadcastMessage> {

    private static final long serialVersionUID = -5090217638134543452L;

    public AssetOptimizedRTMConsumable(AssetOptimizedBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("author.activity/%s/%s",
                             content.getActivityId(),
                             new AssetOptimizedRTMEventDecoratorImpl(new AssetOptimizedRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return String.format("author.activity/%s", content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetOptimizedRTMEventDecoratorImpl(new AssetOptimizedRTMEvent());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
