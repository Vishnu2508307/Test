package data;

import java.util.LinkedList;
import java.util.UUID;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

public class PatchRequest {

    private UUID id;

    private String clientId;

    private LinkedList<DiffSyncPatch> patches;

    private Version n;

    private Version m;

    public UUID getId() {
        return id;
    }

    public PatchRequest setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public PatchRequest setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    public LinkedList<DiffSyncPatch> getPatches() {
        return patches;
    }

    public PatchRequest setPatches(final LinkedList<DiffSyncPatch> patches) {
        this.patches = patches;
        return this;
    }

    public Version getN() {
        return n;
    }

    public PatchRequest setN(final Version n) {
        this.n = n;
        return this;
    }

    public Version getM() {
        return m;
    }

    public PatchRequest setM(final Version m) {
        this.m = m;
        return this;
    }

    @Override
    public String toString() {
        return "PatchRequest{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", patches=" + patches +
                ", n=" + n +
                ", m=" + m +
                '}';
    }
}
