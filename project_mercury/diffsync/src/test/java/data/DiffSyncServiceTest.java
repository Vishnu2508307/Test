package data;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import reactor.core.publisher.Mono;

public class DiffSyncServiceTest {

    @InjectMocks
    DiffSyncService diffSyncService;
    @Mock
    ServerText serverText;
    @Mock
    DiffSyncProvider diffSyncProvider;
    @Mock
    ServerBackup serverBackup;
    @Mock
    ServerShadow serverShadow;
    @Mock
    Channel channel;
    @Mock
    private DiffSyncEntity diffSyncEntity;
    @Mock
    private DiffSyncGateway diffSyncGateway;

    private DiffSyncIdentifier diffSyncIdentifier;
    @Mock
    private Map<EntityType, Provider<SynchronizableService>> synchronizableServiceProviders;
    @Mock
    private Provider<SynchronizableService> provider;
    @Mock
    private SynchronizableService synchronizableService;

    private static final String clientId = UUID.randomUUID().toString();
    private static final UUID entityId = UUID.randomUUID();
    private static final String serverId = "aa-1234-de-34556";
    private List<Patch> patches;
    private Version mVersion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mVersion = new Version().setValue(Long.valueOf(1));

        patches = new ArrayList<>();
        diffSyncEntity = new DiffSyncEntity().setEntityType(EntityType.ACTIVITY_CONFIG).setEntityId(entityId);
        diffSyncIdentifier = new DiffSyncIdentifier()
                .setType(DiffSyncIdentifierType.CLIENT)
                .setServerId(serverId)
                .setClientId(clientId);

        ConcurrentHashMap<String, ConcurrentHashMap<String, DiffSync>> diffSyncEntityMap= new ConcurrentHashMap<>();
        ConcurrentHashMap<String, DiffSync> diffSyncMap = new ConcurrentHashMap<>();
        DiffSync diffSync1 = new DiffSync(serverShadow,serverBackup,channel, diffSyncEntity, diffSyncIdentifier, diffSyncGateway);
        diffSyncMap.put(diffSyncIdentifier.getUrn(), diffSync1);
        diffSyncEntityMap.put(diffSyncEntity.getEntity(), diffSyncMap);
        when(diffSyncProvider.getDiffSyncs()).thenReturn(diffSyncEntityMap);
        when(synchronizableServiceProviders.get(any(EntityType.class))).thenReturn(provider);
        when(provider.get()).thenReturn(synchronizableService);

        diffSyncService = new DiffSyncService(serverText, diffSyncProvider, synchronizableServiceProviders, diffSyncGateway);
    }

    @Test
    public void testSyncPatch(){
        when(synchronizableService.persist(diffSyncEntity, serverText.getContent())).thenReturn(Mono.empty());
        when(serverShadow.diff(any(Patchable.class))).thenReturn(new Patch());
        Patch patch = diffSyncService.syncPatch(diffSyncEntity, patches, diffSyncIdentifier).block();
        assertNotNull(patch);
    }

    @Test
    public void testSyncAck(){
        when(serverShadow.getM()).thenReturn(mVersion);
        when(serverBackup.getM()).thenReturn(mVersion);
        Ack ack = diffSyncService.syncAck(diffSyncEntity , diffSyncIdentifier, new Ack()).block();
        assertNotNull(ack);
    }

}
