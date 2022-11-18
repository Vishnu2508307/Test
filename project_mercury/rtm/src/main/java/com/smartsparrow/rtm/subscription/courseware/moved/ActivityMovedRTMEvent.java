package com.smartsparrow.rtm.subscription.courseware.moved;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityMovedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_MOVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ACTIVITY_MOVED";
    }
}
