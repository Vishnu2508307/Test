package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes assets removed event
 */
public class AssetsRemovedRTMConsumable extends AbstractRTMConsumable<ActivityBroadcastMessage> {

    private static final long serialVersionUID = -2200464106317110156L;

    public AssetsRemovedRTMConsumable(RTMClientContext rtmClientContext, ActivityBroadcastMessage content) {
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
                             new AssetsRemovedRTMEventDecoratorImpl(new AssetsRemovedRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetsRemovedRTMEventDecoratorImpl(new AssetsRemovedRTMEvent());
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
