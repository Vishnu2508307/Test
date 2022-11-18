package data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.util.UUIDs;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import reactor.core.publisher.Mono;


/**
 * Holds the main logic for differential synchronization
 */
public class DiffSync {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSync.class);

    private final ServerShadow serverShadow;
    private final DiffSyncEntity diffSyncEntity;
    // unique identifier name for client or server
    private final DiffSyncIdentifier diffSyncIdentifier;
    private final ServerBackup serverBackup;
    private final Channel channel;
    private List<Patch> editPatchList;
    private DiffSyncGateway diffSyncGateway;

    @Inject
    public DiffSync(final ServerShadow serverShadow,
                    final ServerBackup serverBackup,
                    final Channel channel,
                    final DiffSyncEntity diffSyncEntity,
                    final DiffSyncIdentifier diffSyncIdentifier,
                    final DiffSyncGateway diffSyncGateway) {
        this.serverShadow = serverShadow;
        this.serverBackup = serverBackup;
        this.channel = channel;
        this.diffSyncEntity = diffSyncEntity;
        editPatchList = new ArrayList<>();
        this.diffSyncIdentifier = diffSyncIdentifier;
        this.diffSyncGateway = diffSyncGateway;
    }

    /**
     * This method handle the ack request and clear the edits up to the server version
     * And copy the server version m  from server shadow to server backup
     *
     * @param ack the client or server acknowledgment message
     */
    public Mono<Ack> handleAck(Ack ack) {
        editPatchList = editPatchList.stream()
                .filter(patch -> patch.getM().compareTo(ack.getM()) <= 0)
                .collect(Collectors.toList());
        log.info("Ack has been handled to clear the edits up to the server version");
        return Mono.just(ack);
    }

    /**
     * This method handles list of patches and based on version apply the patches on server shadow, backup and server text
     * and send the message through channel
     * Also create diff between the server shadow and server text and send the diff patch to the channel.
     * If version doesn't match then rollback or throw exception
     *
     * @param patches the list of edit patches from server or client
     * @param serverText the server text document
     */
    public Patch handlePatch(List<Patch> patches, ServerText serverText) {
        // first check server is matching
        for (int i = 0; i < patches.size(); i++) {
            Patch patch = patches.get(i);
            if (patch.getM().compareTo(serverShadow.getM()) >= 0) {
                // check on client version
                if (patch.getN().compareTo(serverShadow.getN()) >= 0) {
                    // apply the patch
                    getServerShadow().apply(patch);
                    // increment client version
                    getServerShadow().incrementNVersion();
                    //apply patch on server text
                    serverText.apply(patch);
                    //update server backup from server shadow
                    getServerBackup().copyPatch(getServerShadow().getContent());
                    //send ack to the client or server after patch done
                    channel.send(Message.build(new Ack()
                                                       .setClientId(getServerShadow().getDiffSyncIdentifier().getClientId())
                                                       .setId(UUIDs.timeBased())
                                                       .setM(getServerShadow().getM())
                                                       .setN(getServerShadow().getN())));
                    log.info("Ack has been sent to client/server");

                    //persist each patch object to the patch summary table
                    diffSyncGateway.savePatch(new PatchSummary(diffSyncEntity.getEntityId(),
                                                               diffSyncEntity.getEntity(),
                                                               patch))
                            .doOnEach(log.reactiveInfo("Patch summary has been saved to diffsync.patch_summary table"))
                            .subscribe();
                    continue;
                }
                // otherwise, nothing to do
            } else {
                // rollback
                // copy the backup content to the shadow
                //update server shadow from server backup
                getServerShadow().copyPatch(getServerBackup().getContent());
                //update server version(m) from the server backup
                getServerShadow().getM().setValue(getServerBackup().getM().getValue());
                // clear all the server edits
                editPatchList = new ArrayList<>();
            }

            // After rollback, if patch server version is still less than server shadow m version then throw exception
            if (patch.getM().compareTo(serverShadow.getM()) < 0) {
                throw new DiffSyncRollbackFault("cannot rollback this far back");
            }

            if (patch.getN().compareTo(serverShadow.getN()) >= 0) {
                // apply patch
                getServerShadow().apply(patch);
                // increment client version n
                getServerShadow().incrementNVersion();
                //apply patch on server text
                serverText.apply(patch);
                //update server backup from server shadow
                getServerBackup().copyPatch(getServerShadow().getContent());
                //send ack to the client or server after patch done
                channel.send(Message.build(new Ack()
                                                   .setClientId(getServerShadow().getDiffSyncIdentifier().getClientId())
                                                   .setId(UUIDs.timeBased())
                                                   .setM(getServerShadow().getM())
                                                   .setN(getServerShadow().getN())));
            }
            // otherwise nothing to do
        }

        // diff between server shadow and server text
        Patch diffPatch = getServerShadow().diff(serverText);
        //increment server version m for server shadow after diff
        getServerShadow().incrementMVersion();
        editPatchList.add(diffPatch);
        // send patch to the channel
        channel.send(Message.build(diffPatch));
        return diffPatch;
    }

    public DiffSyncEntity getDiffSyncEntity() {
        return diffSyncEntity;
    }

    public DiffSyncIdentifier getDiffSyncIdentifier() {
        return diffSyncIdentifier;
    }

    public ServerShadow getServerShadow() {
        return serverShadow;
    }

    public ServerBackup getServerBackup() {
        return serverBackup;
    }

    public Channel getChannel() {
        return channel;
    }
}
