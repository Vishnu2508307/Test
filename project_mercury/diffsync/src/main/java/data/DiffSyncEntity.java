package data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class DiffSyncEntity implements Serializable {
    private static final long serialVersionUID = -4014773834534257820L;

    private EntityType entityType;
    private UUID entityId;

    public EntityType getEntityType() {
        return entityType;
    }

    public DiffSyncEntity setEntityType(final EntityType entityNameType) {
        this.entityType = entityNameType;
        return this;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public DiffSyncEntity setEntityId(final UUID entityId) {
        this.entityId = entityId;
        return this;
    }

    /**
     * Create unique entity for diff sync
     * @return entity string
     */
    public String getEntity() {
        return String.format("entity:%s:%s", getEntityType(), getEntityId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncEntity that = (DiffSyncEntity) o;
        return Objects.equals(entityType, that.entityType) &&
                Objects.equals(entityId, that.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, entityId);
    }

    @Override
    public String toString() {
        return "DiffSyncEntity{" +
                "entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                '}';
    }
}
