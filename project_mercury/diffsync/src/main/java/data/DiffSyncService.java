package data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import reactor.core.publisher.Mono;

@Singleton
public class DiffSyncService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncService.class);

    private final ServerText serverText;
    private final DiffSyncProvider diffSyncProvider;
    private final Map<EntityType, Provider<SynchronizableService>> synchronizableServiceProviders;
    private final DiffSyncGateway diffSyncGateway;

    @Inject
    public DiffSyncService(final ServerText serverText,
                           final DiffSyncProvider diffSyncProvider,
                           final Map<EntityType, Provider<SynchronizableService>> synchronizableServiceProviders,
                           final DiffSyncGateway diffSyncGateway) {
        this.serverText = serverText;
        this.diffSyncProvider = diffSyncProvider;
        this.synchronizableServiceProviders = synchronizableServiceProviders;
        this.diffSyncGateway = diffSyncGateway;
    }

    /**
     * Sync Patch request
     *
     * @param diffSyncEntity the entity info
     * @param diffSyncIdentifier the diff sync identifier
     * @param patches the list of patch request
     * @return mono of diff patch
     */
    public Mono<Patch> syncPatch(final DiffSyncEntity diffSyncEntity,
                                 final List<Patch> patches,
                                 final DiffSyncIdentifier diffSyncIdentifier) {
        ConcurrentHashMap<String, DiffSync> diffSyncMap = diffSyncProvider.getDiffSyncs().get(diffSyncEntity.getEntity());
        DiffSync diffSync = diffSyncMap.get(diffSyncIdentifier.getUrn());
        Patch diffPatch = diffSync.handlePatch(patches, serverText);
        //fetch server text content and persist the updated content into DB
        if(synchronizableServiceProviders.get(diffSyncEntity.getEntityType()) != null) {
            SynchronizableService synchronizableService = synchronizableServiceProviders.get(diffSyncEntity.getEntityType()).get();
            synchronizableService.persist(diffSyncEntity, serverText.getContent())
                    .doOnEach(log.reactiveInfo("Updated server content has been saved as config"))
                    .subscribe();
        } else {
            throw new DiffSyncProviderFault(String.format("Diff Sync service provider is not initialized with entity %s",
                                                         diffSyncEntity.getEntityType()));
        }
        return Mono.just(diffPatch);
    }

    /**
     * Diff sync ack
     *
     * @param diffSyncEntity the entity info
     * @param diffSyncIdentifier the diff sync identifier
     * @param ack the ack object
     */
    public Mono<Ack> syncAck(final DiffSyncEntity diffSyncEntity,
                             final DiffSyncIdentifier diffSyncIdentifier,
                             final Ack ack) {
        ConcurrentHashMap<String, DiffSync> diffSyncMap = diffSyncProvider.getDiffSyncs().get(diffSyncEntity.getEntity());
        DiffSync diffSync = diffSyncMap.get(diffSyncIdentifier.getUrn());
        return diffSync.handleAck(ack);
    }

    /**
     * Start the diff sync
     * Diff sync can be started by client or server
     *
     * @param diffSyncEntity the diff sync entity info
     * @param channel the channel info. either rtm or redis
     * @param diffSyncIdentifier the diff sync identifier

     * @return mono of diff sync
     */
    public Mono<DiffSync> start(final DiffSyncEntity diffSyncEntity,
                                final Channel channel,
                                final DiffSyncIdentifier diffSyncIdentifier) {
        // first validate the binding exists, throw otherwise
        if(synchronizableServiceProviders.get(diffSyncEntity.getEntityType()) != null) {
            SynchronizableService synchronizableService = synchronizableServiceProviders.get(diffSyncEntity.getEntityType()).get();
            return synchronizableService.getEntity(diffSyncEntity)
                    //create diff sync stack
                    .map(config -> new DiffSync(
                            new ServerShadow(diffSyncIdentifier, config, new Version(), new Version()),
                            new ServerBackup(diffSyncIdentifier, config, new Version()),
                            channel,
                            diffSyncEntity,
                            diffSyncIdentifier,
                            diffSyncGateway))
                    //save the new diff sync stack to the provider
                    .map(diffSync -> {
                        diffSyncProvider.add(diffSyncEntity.getEntity(), diffSyncIdentifier.getUrn(), diffSync);
                        //copy latest config to server text content
                        serverText.copyPatch(diffSync.getServerBackup().getContent());
                        return diffSync;
                    });
        } else {
            throw new DiffSyncProviderFault(String.format("Diff Sync service provider is not initialized with entity %s",
                                                          diffSyncEntity.getEntityType()));
        }
    }

    /**
     * End the diff Sync
     *
     * @param diffSyncEntity the entity info
     * @return mono of void
     */
    public Mono<Void> end(final DiffSyncEntity diffSyncEntity) {
        diffSyncProvider.remove(diffSyncEntity.getEntity());
        return Mono.empty();
    }
}
