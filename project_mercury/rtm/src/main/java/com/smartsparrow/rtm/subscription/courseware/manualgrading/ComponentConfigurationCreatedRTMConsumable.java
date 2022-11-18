package com.smartsparrow.rtm.subscription.courseware.manualgrading;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ComponentManualGradingBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes a component manual grading configuration created event
 */
public class ComponentConfigurationCreatedRTMConsumable extends AbstractRTMConsumable<ComponentManualGradingBroadcastMessage> {

    private static final long serialVersionUID = -1558533551290885923L;

    public ComponentConfigurationCreatedRTMConsumable(RTMClientContext rtmClientContext, ComponentManualGradingBroadcastMessage content) {
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
        return new ComponentConfigurationCreatedRTMEvent();
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
