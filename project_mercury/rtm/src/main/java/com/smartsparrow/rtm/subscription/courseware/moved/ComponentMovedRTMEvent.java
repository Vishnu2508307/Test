package com.smartsparrow.rtm.subscription.courseware.moved;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ComponentMovedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COMPONENT_MOVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "COMPONENT_MOVED";
    }
}
