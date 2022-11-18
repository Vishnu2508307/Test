package com.smartsparrow.ingestion.subscription;

import com.smartsparrow.pubsub.data.RTMEvent;

public class IngestionRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PROJECT_INGESTION";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "PROJECT_INGESTION";
    }
}
