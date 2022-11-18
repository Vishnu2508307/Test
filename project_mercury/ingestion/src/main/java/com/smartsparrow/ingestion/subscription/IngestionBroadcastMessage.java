package com.smartsparrow.ingestion.subscription;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.ingestion.data.IngestionStatus;


public class IngestionBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -7299147204968022507L;

    private UUID ingestionId;
    private IngestionStatus ingestionStatus;

    public UUID getIngestionId() {
        return ingestionId;
    }

    public IngestionBroadcastMessage setIngestionId(UUID ingestionId) {
        this.ingestionId = ingestionId;
        return this;
    }

    public IngestionStatus getIngestionStatus() {
        return ingestionStatus;
    }

    public IngestionBroadcastMessage setIngestionStatus(final IngestionStatus ingestionStatus) {
        this.ingestionStatus = ingestionStatus;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionBroadcastMessage that = (IngestionBroadcastMessage) o;
        return Objects.equals(ingestionId, that.ingestionId) &&
                Objects.equals(ingestionStatus, that.ingestionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingestionId, ingestionStatus);
    }

    @Override
    public String toString() {
        return "IngestionBroadcastMessage{" +
                "ingestionId=" + ingestionId +
                ", ingestionStatus=" + ingestionStatus +
                '}';
    }
}
