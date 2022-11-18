package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class IngestionRootGenericMessage extends ReceivedMessage implements IngestionRootElementMessage {

    private UUID rootElementId;

    public UUID getRootElementId() {
        return rootElementId;
    }

    public IngestionRootGenericMessage setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    private UUID projectId;

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionRootGenericMessage that = (IngestionRootGenericMessage) o;
        return rootElementId.equals(that.rootElementId) && projectId.equals(that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, projectId);
    }

    @Override
    public String toString() {
        return "IngestionRootGenericMessage{" +
                "rootElementId=" + rootElementId +
                ", projectId=" + projectId +
                '}';
    }

    public UUID getProjectId() {
        return projectId;
    }


}
