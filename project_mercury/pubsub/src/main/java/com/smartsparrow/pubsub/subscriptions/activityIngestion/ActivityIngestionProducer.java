package com.smartsparrow.pubsub.subscriptions.activityIngestion;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;

/**
 * This  producer produces an event for a activity ingestion
 */
public class ActivityIngestionProducer extends AbstractProducer<ActivityIngestionConsumable> {

    private ActivityIngestionConsumable activityIngestionConsumable;

    @Inject
    public ActivityIngestionProducer() {
    }

    public ActivityIngestionProducer buildIngestionConsumable(UUID ingestionId, UUID projectId, UUID rootElementId,
                                                              Object ingestionStatus) {
        this.activityIngestionConsumable = new ActivityIngestionConsumable(new ActivityIngestionBroadcastMessage(projectId,
                                                                                                                 ingestionId,
                                                                                                                 rootElementId,
                                                                                                                 ingestionStatus));
        return this;
    }

    @Override
    public ActivityIngestionConsumable getEventConsumable() {
        return activityIngestionConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityIngestionProducer that = (ActivityIngestionProducer) o;
        return Objects.equals(activityIngestionConsumable, that.activityIngestionConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityIngestionConsumable);
    }

    @Override
    public String toString() {
        return "IngestionProducer{" +
                "activityIngestionConsumable=" + activityIngestionConsumable +
                '}';
    }
}
