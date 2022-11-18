package com.smartsparrow.cache.diffsync;

import java.util.Objects;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import data.DiffSyncEntity;
import data.DiffSyncIdentifier;

public class DiffSyncSubscription extends AbstractDiffSyncSubscription {
    private static final long serialVersionUID = -5014773865434251040L;

    private final DiffSyncEntity diffSyncEntity;
    private final DiffSyncIdentifier diffSyncIdentifier;

    public interface DiffSyncSubscriptionFactory {
        DiffSyncSubscription create(final DiffSyncEntity diffSyncEntity, final DiffSyncIdentifier diffSyncIdentifier);
    }

    @Inject
    public DiffSyncSubscription(@Assisted final DiffSyncEntity diffSyncEntity,
                                @Assisted final DiffSyncIdentifier diffSyncIdentifier) {
       this.diffSyncEntity = diffSyncEntity;
        this.diffSyncIdentifier = diffSyncIdentifier;
    }

    @Override
    public String getName() {
        return String.format("diff:sync:%s:%s", getDiffSyncEntity().getEntityType(), getDiffSyncEntity().getEntityId());
    }

    public DiffSyncEntity getDiffSyncEntity() {
        return diffSyncEntity;
    }

    public DiffSyncIdentifier getDiffSyncIdentifier() {
        return diffSyncIdentifier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncSubscription that = (DiffSyncSubscription) o;
        return Objects.equals(diffSyncEntity, that.diffSyncEntity) &&
                Objects.equals(diffSyncIdentifier, that.diffSyncIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffSyncEntity, diffSyncIdentifier);
    }

    @Override
    public String toString() {
        return "DiffSyncSubscription{" +
                "diffSyncEntity=" + diffSyncEntity +
                ", diffSyncIdentifier=" + diffSyncIdentifier +
                '}';
    }
}
