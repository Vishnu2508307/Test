package com.smartsparrow.rtm.subscription.courseware.scenarioreordered;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ScenarioReOrderedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "SCENARIO_REORDERED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "SCENARIO_REORDERED";
    }
}
