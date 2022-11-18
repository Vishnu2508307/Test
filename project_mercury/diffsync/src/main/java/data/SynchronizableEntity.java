package data;

import java.util.Objects;
import java.util.UUID;

public class SynchronizableEntity {

    private final EntityType entityType;
    private final UUID id;

    public SynchronizableEntity(EntityType entityType, UUID id) {
        this.entityType = entityType;
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SynchronizableEntity that = (SynchronizableEntity) o;
        return entityType == that.entityType && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, id);
    }
}
