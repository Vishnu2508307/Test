package com.smartsparrow.rtm.subscription.cohort.enrolled;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;

import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes a cohort enrolled event
 */
public class CohortEnrolledRTMConsumable extends AbstractRTMConsumable<CohortBroadcastMessage> {

    private static final long serialVersionUID = -1084967799691724826L;

    public CohortEnrolledRTMConsumable(RTMClientContext rtmClientContext, CohortBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("workspace.cohort/subscription/%s/%s", content.cohortId, getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return CohortRTMSubscription.NAME(content.cohortId);
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new CohortEnrolledRTMEvent();
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
