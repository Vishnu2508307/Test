package com.smartsparrow.rtm.message.recv.diffsync;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import data.EntityType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DiffSyncStartMessage extends ReceivedMessage implements DiffSyncMessage {

    private EntityType entityType;
    private UUID entityId;

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncStartMessage that = (DiffSyncStartMessage) o;
        return entityType == that.entityType &&
                Objects.equals(entityId, that.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, entityId);
    }

    @Override
    public String toString() {
        return "DiffSyncStartMessage{" +
                "entityType=" + entityType +
                ", entityId=" + entityId +
                '}';
    }
}
