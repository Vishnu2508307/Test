package com.smartsparrow.rtm.subscription.courseware.assetoptimized;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.AssetOptimizedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an asset optimized event
 */
public class AssetOptimizedRTMConsumable extends AbstractRTMConsumable<AssetOptimizedBroadcastMessage> {

    private static final long serialVersionUID = -2315700881413093231L;

    public AssetOptimizedRTMConsumable(RTMClientContext rtmClientContext, AssetOptimizedBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("author.activity/%s/%s",
                             content.getActivityId(),
                             new AssetOptimizedRTMEventDecoratorImpl(new AssetOptimizedRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
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
