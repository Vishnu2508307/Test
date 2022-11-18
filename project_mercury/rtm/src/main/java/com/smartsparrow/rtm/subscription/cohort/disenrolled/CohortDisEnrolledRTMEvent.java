package com.smartsparrow.rtm.subscription.cohort.disenrolled;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortDisEnrolledRTMEvent implements RTMEvent {

    @Override
    public String getName() {
        return "COHORT_DISENROLLED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "DISENROLLED";
    }
}
