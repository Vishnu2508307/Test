package com.smartsparrow.rtm.subscription.courseware.assetadded;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AssetAddedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ASSET_ADDED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ASSET_ADDED";
    }
}
