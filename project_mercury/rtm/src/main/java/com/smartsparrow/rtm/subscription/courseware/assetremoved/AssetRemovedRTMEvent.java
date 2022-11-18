package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AssetRemovedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ASSET_REMOVED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ASSET_REMOVED";
    }
}
