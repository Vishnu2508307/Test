package com.smartsparrow.rtm.subscription.workspace;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ProjectCreatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PROJECT_CREATED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "CREATED";
    }
}
