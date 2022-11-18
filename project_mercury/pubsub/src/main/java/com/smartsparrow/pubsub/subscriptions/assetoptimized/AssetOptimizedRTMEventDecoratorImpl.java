package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AssetOptimizedRTMEventDecoratorImpl extends AssetOptimizedRTMEventDecorator {

    public AssetOptimizedRTMEventDecoratorImpl(RTMEvent rtmEvent) {
        super(rtmEvent);
    }

    public String getName(Object elementType) {
        return elementType.toString() + "_" + super.getName();
    }

    public Boolean equalsTo(RTMEvent rtmEvent) {
        return super.equalsTo(rtmEvent);
    }

    public String getLegacyName() {
        return super.getLegacyName();
    }
}
