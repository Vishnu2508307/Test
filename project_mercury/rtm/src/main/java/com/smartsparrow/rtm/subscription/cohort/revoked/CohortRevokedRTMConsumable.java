package com.smartsparrow.rtm.subscription.cohort.revoked;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes a cohort revoked event
 */
public class CohortRevokedRTMConsumable extends AbstractRTMConsumable<CohortBroadcastMessage> {

    private static final long serialVersionUID = -1234967799691724826L;

    public CohortRevokedRTMConsumable(RTMClientContext rtmClientContext, CohortBroadcastMessage content) {
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
        return new CohortRevokedRTMEvent();
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
