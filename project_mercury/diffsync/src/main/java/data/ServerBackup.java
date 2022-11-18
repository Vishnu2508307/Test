package data;

import javax.inject.Inject;

import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Describes the server backup
 */
public class ServerBackup extends DefaultPatchableImplementation {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ServerBackup.class);

    private String content;
    private Version mVersion;
    private DiffSyncIdentifier diffSyncIdentifier;

    @Inject
    public ServerBackup(final DiffSyncIdentifier diffSyncIdentifier, final String content, final Version mVersion) {
        this.diffSyncIdentifier = diffSyncIdentifier;
        this.content = content;
        this.mVersion = mVersion;
        mVersion.setValue(Long.valueOf(0));
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
    public synchronized Patchable copyPatch(final String content) {
        this.content = content;
        return this;
    }

    //no need to store client version in backup
    @Override
    public Version getN() {
        log.info("Server backup doesn't support client version");
        throw new UnsupportedOperationFault("Server backup doesn't support client version");
    }

    @Override
    public Version getM() {
        return mVersion;
    }

}
