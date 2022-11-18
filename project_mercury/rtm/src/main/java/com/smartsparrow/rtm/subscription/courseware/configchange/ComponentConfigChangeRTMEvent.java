package com.smartsparrow.rtm.subscription.courseware.configchange;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ComponentConfigChangeRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COMPONENT_CONFIG_CHANGE";
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
