package com.smartsparrow.rtm.subscription.courseware.updated;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ScenarioUpdatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "SCENARIO_UPDATED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "UPDATED";
    }
}
