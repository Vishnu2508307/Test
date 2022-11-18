package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.ingestion.data.IngestionAdapterType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ProjectIngestionStartMessage extends IngestionGenericMessage {

    private IngestionAdapterType adapterType;

    public IngestionAdapterType getAdapterType() {
        return adapterType;
    }

    public ProjectIngestionStartMessage setAdapterType(final IngestionAdapterType adapter) {
        this.adapterType = adapter;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectIngestionStartMessage that = (ProjectIngestionStartMessage) o;
        return  adapterType.equals(that.adapterType) &&
                getIngestionId().equals(that.getIngestionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIngestionId(), adapterType);
    }

    @Override
    public String toString() {
        return "ProjectIngestionStartMessage{" +
                ", ingestionId=" + getIngestionId() +
                ", adapterType=" + adapterType +
                '}';
    }
}
