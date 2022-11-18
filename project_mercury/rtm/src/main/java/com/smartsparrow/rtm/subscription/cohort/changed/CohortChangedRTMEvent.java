package com.smartsparrow.rtm.subscription.cohort.changed;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortChangedRTMEvent implements RTMEvent {

    @Override
    public String getName() {
        return "COHORT_CHANGED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "CHANGED";
    }
}
