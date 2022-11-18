package com.smartsparrow.rtm.message.recv.diffsync;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import data.EntityType;
import data.Version;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DiffSyncAckMessage extends ReceivedMessage implements DiffSyncMessage {

    private EntityType entityType;
    private UUID entityId;
    private Version n;
    private Version m;

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    public Version getN() {
        return n;
    }

    public Version getM() {
        return m;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncAckMessage that = (DiffSyncAckMessage) o;
        return entityType == that.entityType &&
                Objects.equals(entityId, that.entityId) &&
                Objects.equals(n, that.n) &&
                Objects.equals(m, that.m);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, entityId, n, m);
    }

    @Override
    public String toString() {
        return "DiffSyncAckMessage{" +
                "entityType=" + entityType +
                ", entityId=" + entityId +
                ", n=" + n +
                ", m=" + m +
                '}';
    }
}
