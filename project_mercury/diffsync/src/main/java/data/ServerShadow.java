package data;

import javax.inject.Inject;

/**
 * Describes the ServerShadow document
 */
public class ServerShadow extends DefaultPatchableImplementation {
    private String content;
    private Version mVersion;
    private Version nVersion;
    private DiffSyncIdentifier diffSyncIdentifier ;

    @Inject
    public ServerShadow(DiffSyncIdentifier diffSyncIdentifier, final String content, final Version mVersion, final Version nVersion) {
        this.diffSyncIdentifier = diffSyncIdentifier;
        this.content = content;
        this.mVersion = mVersion;
        this.nVersion = nVersion;
        mVersion.setValue(Long.valueOf(0));
        nVersion.setValue(Long.valueOf(0));
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public DiffSyncIdentifier getDiffSyncIdentifier() {
        return diffSyncIdentifier;
    }

    @Override
    public Version getN() {
        return nVersion;
    }

    @Override
    public Version getM() {
        return mVersion;
    }

    @Override
    public synchronized Patchable copyPatch(final String content) {
        this.content = content;
        return this;
    }

    public void incrementNVersion() {
        long value = nVersion.getValue().longValue();
        nVersion.setValue(value + 1);
    }

    public void incrementMVersion() {
        long value = mVersion.getValue().longValue();
        mVersion.setValue(value + 1);
    }


}
