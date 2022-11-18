package com.smartsparrow.rtm.subscription.cohort.unarchived;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CohortUnArchivedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COHORT_UNARCHIVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "UNARCHIVED";
    }
}
