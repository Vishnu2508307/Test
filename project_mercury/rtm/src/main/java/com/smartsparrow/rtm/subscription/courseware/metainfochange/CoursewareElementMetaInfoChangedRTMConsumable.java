package com.smartsparrow.rtm.subscription.courseware.metainfochange;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes a courseware element meta info change event
 */
public class CoursewareElementMetaInfoChangedRTMConsumable extends AbstractRTMConsumable<ActivityBroadcastMessage> {

    private static final long serialVersionUID = 7311011852724800871L;

    public CoursewareElementMetaInfoChangedRTMConsumable(RTMClientContext rtmClientContext, ActivityBroadcastMessage content) {
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
        return new CoursewareElementMetaInfoChangedRTMEvent();
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
