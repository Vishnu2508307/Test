package com.smartsparrow.rtm.subscription.courseware.manualgrading;


import com.smartsparrow.pubsub.data.RTMEvent;

public class ComponentManualGradingConfigDeletedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "MANUAL_GRADING_CONFIGURATION_DELETED";
    }
}
