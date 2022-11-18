package com.smartsparrow.rtm.subscription.cohort.revoked;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortRevokedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COHORT_REVOKED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "REVOKED";
    }
}
