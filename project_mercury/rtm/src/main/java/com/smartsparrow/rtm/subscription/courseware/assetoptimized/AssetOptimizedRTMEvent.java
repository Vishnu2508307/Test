package com.smartsparrow.rtm.subscription.courseware.assetoptimized;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AssetOptimizedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ASSET_OPTIMIZED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ASSET_OPTIMIZED";
    }
}
