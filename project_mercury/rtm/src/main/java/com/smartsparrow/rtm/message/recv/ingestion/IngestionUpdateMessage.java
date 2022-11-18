package com.smartsparrow.rtm.message.recv.ingestion;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.ingestion.data.IngestionStatus;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class IngestionUpdateMessage extends IngestionGenericMessage {

    private UUID projectId;
    private IngestionStatus status;

    public UUID getProjectId() {
        return projectId;
    }

    public IngestionUpdateMessage setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public IngestionStatus getStatus() {
        return status;
    }

    public IngestionUpdateMessage setStatus(final IngestionStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionUpdateMessage that = (IngestionUpdateMessage) o;
        return Objects.equals(status, that.status) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(getIngestionId(), that.getIngestionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIngestionId(), projectId, status);
    }

    @Override
    public String toString() {
        return "IngestionUpdateMessage{" +
                "ingestionId=" + getIngestionId() +
                "projectId=" + projectId +
                "status='" + status + "'" +
                '}';
    }
}
