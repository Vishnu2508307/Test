package com.smartsparrow.cache.diffsync;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.Message;

public class DiffSyncProducer extends AbstractDiffSyncProducer {

    @Inject
    public DiffSyncProducer() {
    }

    private DiffSyncMessage diffSyncMessage;

    public DiffSyncProducer buildConsumableMessage(final Message message,
                                                   final DiffSyncIdentifier diffSyncIdentifier,
                                                   final DiffSyncEntity diffSyncEntity) {
        this.diffSyncMessage = new DiffSyncMessage(message, diffSyncIdentifier, diffSyncEntity);
        return this;
    }

    @Override
    public DiffSyncMessage getDiffSyncMessage() {
        return diffSyncMessage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncProducer that = (DiffSyncProducer) o;
        return Objects.equals(diffSyncMessage, that.diffSyncMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffSyncMessage);
    }

    @Override
    public String toString() {
        return "DiffSyncProducer{" +
                "diffSyncMessage=" + diffSyncMessage +
                '}';
    }
}
