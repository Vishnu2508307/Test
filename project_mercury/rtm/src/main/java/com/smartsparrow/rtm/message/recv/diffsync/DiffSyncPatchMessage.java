package com.smartsparrow.rtm.message.recv.diffsync;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import data.EntityType;
import data.Patch;
import data.PatchRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DiffSyncPatchMessage extends ReceivedMessage implements DiffSyncMessage{

    private EntityType entityType;
    private UUID entityId;
    private List<Patch> patches;

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    public List<Patch> getPatches() {
        return patches;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncPatchMessage that = (DiffSyncPatchMessage) o;
        return entityType == that.entityType &&
                Objects.equals(entityId, that.entityId) &&
                Objects.equals(patches, that.patches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, entityId, patches);
    }

    @Override
    public String toString() {
        return "DiffSyncPatchMessage{" +
                "entityType=" + entityType +
                ", entityId=" + entityId +
                ", patches=" + patches +
                '}';
    }
}
