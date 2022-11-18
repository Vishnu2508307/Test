package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityAssetsRemovedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_ASSETS_REMOVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ASSETS_REMOVED";
    }
}
