package com.smartsparrow.rtm.subscription.cohort.granted;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortGrantedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COHORT_GRANTED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "GRANTED";
    }
}
