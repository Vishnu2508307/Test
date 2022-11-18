package data;

import javax.inject.Inject;

import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Represents the Server Text document
 */
public class ServerText extends DefaultPatchableImplementation {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ServerText.class);

    private volatile String content;
    private DiffSyncIdentifier diffSyncIdentifier;

    @Inject
    public ServerText(final DiffSyncIdentifier diffSyncIdentifier, final String content) {
        this.diffSyncIdentifier = diffSyncIdentifier;
        this.content = content;
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

    //no need to store client version in server text document
    @Override
    public Version getN() {
        log.info("Server text object doesn't save client version");
        throw new UnsupportedOperationFault("Server text object doesn't support client version");
    }

    //no need to store server version in server text document
    @Override
    public Version getM() {
        log.info("Server text object doesn't save server version");
        throw new UnsupportedOperationFault("Server text object doesn't support server version");
    }

}
