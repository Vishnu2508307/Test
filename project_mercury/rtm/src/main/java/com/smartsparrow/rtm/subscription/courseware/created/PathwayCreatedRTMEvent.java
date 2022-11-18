package com.smartsparrow.rtm.subscription.courseware.created;

import com.smartsparrow.pubsub.data.RTMEvent;

public class PathwayCreatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PATHWAY_CREATED";
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
