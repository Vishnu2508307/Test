package data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Describes what the Patch object looks like
 */
public class Patch implements Exchangeable, Serializable{

    private static final long serialVersionUID = 3897179687824124010L;
    /**
     * A unique time-based identifier
     */
    private UUID id;
    /**
     * The clientId producing this patch (could be the server producing it)
     */
    private String clientId;
    /**
     * A list of diffs this patch contains
     */

    private LinkedList<DiffMatchPatchCustom.Patch> patches;
    /**
     * The n version at the time the patch was produced
     */
    private Version n;
    /**
     * The m version at the time the patch was produced
     */
    private Version m;

    public UUID getId() {
        return id;
    }

    public Patch setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public Patch setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public LinkedList<DiffMatchPatchCustom.Patch> getPatches() {
        return patches;
    }

    public Patch setPatches(LinkedList<DiffMatchPatchCustom.Patch> patches) {
        this.patches = patches;
        return this;
    }

    public Version getN() {
        return n;
    }

    public Patch setN(Version n) {
        this.n = n;
        return this;
    }

    public Version getM() {
        return m;
    }

    public Patch setM(Version m) {
        this.m = m;
        return this;
    }

    @Override
    @JsonIgnore
    public Type getType() {
        return Type.PATCH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patch patch = (Patch) o;
        return Objects.equals(id, patch.id)
                && Objects.equals(clientId, patch.clientId)
                && Objects.equals(patches, patch.patches)
                && Objects.equals(n, patch.n)
                && Objects.equals(m, patch.m);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clientId, patches, n, m);
    }

    @Override
    public String toString() {
        return "Patch{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", patches=" + patches +
                ", n=" + n +
                ", m=" + m +
                '}';
    }
}
