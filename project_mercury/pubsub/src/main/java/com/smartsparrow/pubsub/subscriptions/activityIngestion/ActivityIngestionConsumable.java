package com.smartsparrow.pubsub.subscriptions.activityIngestion;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This consumable describes an activity project ingestion
 */
public class ActivityIngestionConsumable extends AbstractConsumable<ActivityIngestionBroadcastMessage> {

    private static final long serialVersionUID = -4942668803175062250L;

    public ActivityIngestionConsumable(ActivityIngestionBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("author.activity/%s/%s", content.getRootElementId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("author.activity/%s", content.getRootElementId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityIngestionRTMEvent();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
