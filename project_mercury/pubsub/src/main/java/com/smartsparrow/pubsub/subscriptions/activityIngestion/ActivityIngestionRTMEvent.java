package com.smartsparrow.pubsub.subscriptions.activityIngestion;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityIngestionRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_INGESTION";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ACTIVITY_INGESTION";
    }
}
