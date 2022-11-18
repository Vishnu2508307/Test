package com.smartsparrow.rtm.subscription.courseware.deleted;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes an activity component deleted event
 */
public class ComponentDeletedRTMConsumable extends AbstractRTMConsumable<ActivityBroadcastMessage> {

    private static final long serialVersionUID = 6210443630502393128L;

    public ComponentDeletedRTMConsumable(RTMClientContext rtmClientContext, ActivityBroadcastMessage content) {
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
        return new ComponentDeletedRTMEvent();
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
