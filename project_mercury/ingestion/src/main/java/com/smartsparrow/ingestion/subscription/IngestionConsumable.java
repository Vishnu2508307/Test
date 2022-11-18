package com.smartsparrow.ingestion.subscription;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This consumable describes an project ingestion
 */
public class IngestionConsumable extends AbstractConsumable<IngestionBroadcastMessage> {

    private static final long serialVersionUID = 323829735248229968L;

    public IngestionConsumable(IngestionBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("project.ingest/%s/%s", content.getIngestionId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("project.ingest/%s", content.getIngestionId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new IngestionRTMEvent();
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
