package com.smartsparrow.rtm.subscription.cohort.enrolled;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortEnrolledRTMEvent implements RTMEvent {

    @Override
    public String getName() {
        return "COHORT_ENROLLED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ENROLLED";
    }
}
