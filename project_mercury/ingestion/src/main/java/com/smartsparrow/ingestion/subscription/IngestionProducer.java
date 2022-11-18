package com.smartsparrow.ingestion.subscription;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.pubsub.data.AbstractProducer;

/**
 * This  producer produces an event for a project ingestion
 */
public class IngestionProducer extends AbstractProducer<IngestionConsumable> {

    private IngestionConsumable ingestionConsumable;

    @Inject
    public IngestionProducer() {
    }

    public IngestionProducer buildIngestionConsumable(UUID ingestionId,
                                                      IngestionStatus ingestionStatus) {
        this.ingestionConsumable = new IngestionConsumable(new IngestionBroadcastMessage()
                                                                   .setIngestionId(ingestionId)
                                                                   .setIngestionStatus(ingestionStatus));
        return this;
    }

    @Override
    public IngestionConsumable getEventConsumable() {
        return ingestionConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionProducer that = (IngestionProducer) o;
        return Objects.equals(ingestionConsumable, that.ingestionConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingestionConsumable);
    }

    @Override
    public String toString() {
        return "IngestionProducer{" +
                "ingestionConsumable=" + ingestionConsumable +
                '}';
    }
}
