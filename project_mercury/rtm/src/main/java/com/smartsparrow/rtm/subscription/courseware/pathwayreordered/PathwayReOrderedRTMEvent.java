package com.smartsparrow.rtm.subscription.courseware.pathwayreordered;

import com.smartsparrow.pubsub.data.RTMEvent;

public class PathwayReOrderedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PATHWAY_REORDERED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "PATHWAY_REORDERED";
    }
}
