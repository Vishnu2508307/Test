package data;

import java.util.LinkedList;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import com.datastax.driver.core.utils.UUIDs;

/**
 * This class is responsible for implementing apply of a patch
 * and diff with another patchable
 */
public abstract class DefaultPatchableImplementation implements Patchable{

    @Override
    public abstract String getContent();

    public abstract DiffSyncIdentifier getDiffSyncIdentifier();

    public abstract Version getN();

    public abstract Version getM();

    public abstract Patchable copyPatch(String content);

    @Override
    public synchronized Patchable apply(Patch patch) {
        DiffMatchPatchCustom diffMatchPatch = new DiffMatchPatchCustom();
        Object[] objects = diffMatchPatch.patchApply(patch.getPatches(), getContent());
        return copyPatch((String)objects[0]);
    }

    @Override
    public synchronized Patch diff(Patchable patchable) {
        DiffMatchPatchCustom diffMatchPatch = new DiffMatchPatchCustom();
        LinkedList<DiffMatchPatchCustom.Patch> patches = diffMatchPatch.patchMake(getContent(), patchable.getContent());

        return new Patch()
                .setClientId(getDiffSyncIdentifier().getClientId()) // this must not be null
                .setPatches(patches)
                .setId(UUIDs.timeBased())
                .setM(getM())
                .setN(getN());
    }

}
