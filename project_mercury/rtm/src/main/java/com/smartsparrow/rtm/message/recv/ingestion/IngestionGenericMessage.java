package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class IngestionGenericMessage extends ReceivedMessage implements IngestionMessage {

    private UUID ingestionId;

    public UUID getIngestionId() {
        return ingestionId;
    }

    public IngestionGenericMessage setIngestionId(final UUID ingestionId) {
        this.ingestionId = ingestionId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionGenericMessage that = (IngestionGenericMessage) o;
        return Objects.equals(ingestionId, that.ingestionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingestionId);
    }

    @Override
    public String toString() {
        return "IngestionGenericMessage{" +
                "ingestionId=" + ingestionId +
                '}';
    }
}
