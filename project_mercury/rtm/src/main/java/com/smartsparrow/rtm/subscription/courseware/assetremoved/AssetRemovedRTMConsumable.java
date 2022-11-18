package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.AssetRemovedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an asset removed event
 */
public class AssetRemovedRTMConsumable extends AbstractRTMConsumable<AssetRemovedBroadcastMessage> {

    private static final long serialVersionUID = -6977362311183699481L;

    public AssetRemovedRTMConsumable(RTMClientContext rtmClientContext, AssetRemovedBroadcastMessage content) {
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
                             new AssetRemovedRTMEventDecoratorImpl(new AssetRemovedRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetRemovedRTMEventDecoratorImpl(new AssetRemovedRTMEvent());
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
