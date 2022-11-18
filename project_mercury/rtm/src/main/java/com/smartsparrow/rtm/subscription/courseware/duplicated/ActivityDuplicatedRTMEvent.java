package com.smartsparrow.rtm.subscription.courseware.duplicated;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityDuplicatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_DUPLICATED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "DUPLICATED";
    }
}
