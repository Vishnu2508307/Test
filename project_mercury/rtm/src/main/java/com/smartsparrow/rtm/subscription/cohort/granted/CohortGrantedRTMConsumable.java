package com.smartsparrow.rtm.subscription.cohort.granted;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes a cohort granted event
 */
public class CohortGrantedRTMConsumable extends AbstractRTMConsumable<CohortBroadcastMessage> {

    private static final long serialVersionUID = -1054967799691724833L;

    public CohortGrantedRTMConsumable(RTMClientContext rtmClientContext, CohortBroadcastMessage content) {
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
        return new CohortGrantedRTMEvent();
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
