package com.smartsparrow.rtm.subscription.courseware.assetadded;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.AssetAddedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an asset added event
 */
public class AssetAddedRTMConsumable extends AbstractRTMConsumable<AssetAddedBroadcastMessage> {

    private static final long serialVersionUID = 1200309336774313528L;

    public AssetAddedRTMConsumable(RTMClientContext rtmClientContext, AssetAddedBroadcastMessage content) {
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
                             new AssetAddedRTMEventDecoratorImpl(new AssetAddedRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetAddedRTMEventDecoratorImpl(new AssetAddedRTMEvent());
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
