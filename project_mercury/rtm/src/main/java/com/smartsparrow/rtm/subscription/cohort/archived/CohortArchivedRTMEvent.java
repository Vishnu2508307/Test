package com.smartsparrow.rtm.subscription.cohort.archived;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortArchivedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COHORT_ARCHIVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ARCHIVED";
    }
}
