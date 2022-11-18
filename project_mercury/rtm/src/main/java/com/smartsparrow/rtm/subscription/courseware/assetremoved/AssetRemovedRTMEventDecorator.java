package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import com.smartsparrow.pubsub.data.RTMEvent;

public abstract class AssetRemovedRTMEventDecorator implements RTMEvent {
    private final RTMEvent rtmEvent;

    public AssetRemovedRTMEventDecorator(final RTMEvent rtmEvent) {
        this.rtmEvent = rtmEvent;
    }

    @Override
    public String getName() {
        return rtmEvent.getName();
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return rtmEvent.equalsTo(rtmEvent);
    }

    @Override
    public String getLegacyName() {
        return rtmEvent.getLegacyName();
    }
}
