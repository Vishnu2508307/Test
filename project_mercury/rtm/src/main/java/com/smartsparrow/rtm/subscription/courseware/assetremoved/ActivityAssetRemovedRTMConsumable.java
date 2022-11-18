package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an activity asset removed event
 */
public class ActivityAssetRemovedRTMConsumable extends AbstractRTMConsumable<ActivityCreatedBroadcastMessage> {

    private static final long serialVersionUID = 8005603769769140630L;

    public ActivityAssetRemovedRTMConsumable(RTMClientContext rtmClientContext, ActivityCreatedBroadcastMessage content) {
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
        return new ActivityAssetRemovedRTMEvent();
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
