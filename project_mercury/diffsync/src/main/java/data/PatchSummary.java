package data;

import java.util.Objects;
import java.util.UUID;

public class PatchSummary {

    private UUID patchId;
    private String clientId;
    private UUID entityId;
    private String entityName;
    private String patches;
    private Version n;
    private Version m;

    public PatchSummary(final UUID entityId, final String entityName, final Patch patch) {
        this.entityId = entityId;
        this.entityName = entityName;
        this.setPatchId(patch.getId());
        this.setClientId(patch.getClientId());
        this.setPatches(patch.getPatches().toString());
        this.setN(patch.getN());
        this.setM(patch.getM());
    }

    public UUID getPatchId() {
        return patchId;
    }

    public data.PatchSummary setPatchId(final UUID patchId) {
        this.patchId = patchId;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public data.PatchSummary setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public data.PatchSummary setEntityId(final UUID entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getEntityName() {
        return entityName;
    }

    public data.PatchSummary setEntityName(final String entityName) {
        this.entityName = entityName;
        return this;
    }

    public String getPatches() {
        return patches;
    }

    public data.PatchSummary setPatches(final String patches) {
        this.patches = patches;
        return this;
    }

    public Version getN() {
        return n;
    }

    public data.PatchSummary setN(final Version n) {
        this.n = n;
        return this;
    }

    public Version getM() {
        return m;
    }

    public data.PatchSummary setM(final Version m) {
        this.m = m;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PatchSummary that = (PatchSummary) o;
        return Objects.equals(patchId, that.patchId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(entityId, that.entityId) &&
                Objects.equals(entityName, that.entityName) &&
                Objects.equals(patches, that.patches) &&
                Objects.equals(n, that.n) &&
                Objects.equals(m, that.m);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), patchId, clientId, entityId, entityName, patches, n, m);
    }

    @Override
    public String toString() {
        return "PatchSummary{" +
                "patchId=" + patchId +
                ", clientId='" + clientId + '\'' +
                ", entityId=" + entityId +
                ", entityName='" + entityName + '\'' +
                ", patches='" + patches + '\'' +
                ", n=" + n +
                ", m=" + m +
                '}';
    }
}
