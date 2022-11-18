package com.smartsparrow.rtm.subscription.courseware.descriptivechange;

import com.smartsparrow.pubsub.data.RTMEvent;

public class DescriptiveChangeRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "DESCRIPTIVE_CHANGE";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "DESCRIPTIVE_CHANGE";
    }
}
