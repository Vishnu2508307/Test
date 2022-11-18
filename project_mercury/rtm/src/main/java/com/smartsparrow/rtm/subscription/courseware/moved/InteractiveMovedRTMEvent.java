package com.smartsparrow.rtm.subscription.courseware.moved;

import com.smartsparrow.pubsub.data.RTMEvent;

public class InteractiveMovedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "INTERACTIVE_MOVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "INTERACTIVE_MOVED";
    }
}
