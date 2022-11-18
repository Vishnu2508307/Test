package com.smartsparrow.rtm.subscription.courseware.moved;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ElementMovedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an activity moved event
 */
public class ActivityMovedRTMConsumable extends AbstractRTMConsumable<ElementMovedBroadcastMessage> {

    private static final long serialVersionUID = -234621980194888163L;

    public ActivityMovedRTMConsumable(RTMClientContext rtmClientContext, ElementMovedBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("author.activity/%s/%s", content.getActivityId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityMovedRTMEvent();
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
