package com.smartsparrow.cache.diffsync;

import java.io.Serializable;
import java.util.Objects;

import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.Exchangeable;
import data.Message;

public class DiffSyncMessage implements Serializable {
    
    private static final long serialVersionUID = 4997179687824223010L;

    private final Message<? extends Exchangeable> message;
    // Unique server or client identifier info
    private final DiffSyncIdentifier diffSyncIdentifier;
    private final DiffSyncEntity diffSyncEntity;


    public DiffSyncMessage(final Message<? extends Exchangeable> message,
                           final DiffSyncIdentifier diffSyncIdentifier,
                           final  DiffSyncEntity diffSyncEntity) {
        this.message = message;
        this.diffSyncIdentifier = diffSyncIdentifier;
        this.diffSyncEntity = diffSyncEntity;
    }

    public Message<? extends Exchangeable> getMessage() {
        return message;
    }

    public DiffSyncIdentifier getDiffSyncIdentifier() {
        return diffSyncIdentifier;
    }

    public DiffSyncEntity getDiffSyncEntity() {
        return diffSyncEntity;
    }

    public String getSubscriptionName() {
        return String.format("diff:sync:%s:%s", getDiffSyncEntity().getEntityType(), getDiffSyncEntity().getEntityId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncMessage that = (DiffSyncMessage) o;
        return Objects.equals(message, that.message) &&
                Objects.equals(diffSyncIdentifier, that.diffSyncIdentifier) &&
                Objects.equals(diffSyncEntity, that.diffSyncEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, diffSyncIdentifier, diffSyncEntity);
    }

    @Override
    public String toString() {
        return "DiffSyncMessage{" +
                "message=" + message +
                ", diffSyncIdentifier=" + diffSyncIdentifier +
                ", diffSyncEntity=" + diffSyncEntity +
                '}';
    }
}
