package com.smartsparrow.rtm.subscription.courseware.configchange;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityConfigChangeRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_CONFIG_CHANGE";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "CONFIG_CHANGE";
    }
}
