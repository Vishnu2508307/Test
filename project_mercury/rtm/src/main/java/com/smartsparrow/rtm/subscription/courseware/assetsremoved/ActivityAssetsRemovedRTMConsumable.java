package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an activity assets removed event
 */
public class ActivityAssetsRemovedRTMConsumable extends AbstractRTMConsumable<ActivityCreatedBroadcastMessage> {

    private static final long serialVersionUID = 5640376897749043729L;

    public ActivityAssetsRemovedRTMConsumable(RTMClientContext rtmClientContext, ActivityCreatedBroadcastMessage content) {
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
        return new ActivityAssetsRemovedRTMEvent();
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
