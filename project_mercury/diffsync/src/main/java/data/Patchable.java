package data;

/**
 * Describes an object that can be diffed and patched
 */
public interface Patchable {

    /**
     *
     * @return the string content of the patchable object
     */
    String getContent();

    /**
     * Applies a patch to this patchable
     *
     * @param patch the patch to apply
     * @return a patchable with the patched content
     */
    Patchable apply(Patch patch);

    /**
     * Computes a diff with another patchable
     *
     * @param patchable the other patchable to compare to this one and compute the diff with
     * @return a Patch describing the diffs between this patchable and the other patchable
     */
    Patch diff(Patchable patchable);

    /**
     * This method copy patch content
     * @param content the patchable content
     * @return a patchable with the updated patched content
     */
    Patchable copyPatch(String content);
}
