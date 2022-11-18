package com.smartsparrow.rtm.subscription.courseware.deleted;

import com.smartsparrow.pubsub.data.RTMEvent;

public class PathwayDeletedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PATHWAY_DELETED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "DELETED";
    }
}
