package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncStartMessageHandler.DIFF_SYNC_START_ERROR;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncStartMessageHandler.DIFF_SYNC_START_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.cache.diffsync.DiffSyncProducer;
import com.smartsparrow.cache.diffsync.DiffSyncSubscription;
import com.smartsparrow.cache.diffsync.DiffSyncSubscriptionManager;
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.diffsync.MessageTypeBridgeConverter;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncMessage;
import com.smartsparrow.rtm.diffsync.RTMChannel;
import com.smartsparrow.rtm.diffsync.RTMChannelFactory;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncStartMessage;

import data.Channel;
import data.DiffSync;
import data.DiffSyncEntity;
import data.DiffSyncGateway;
import data.DiffSyncIdentifier;
import data.DiffSyncIdentifierType;
import data.DiffSyncService;
import data.EntityType;
import data.Message;
import data.ServerBackup;
import data.ServerShadow;
import data.Version;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DiffSyncStartMessageHandlerTest {
    private Session session;

    @InjectMocks
    DiffSyncStartMessageHandler handler;

    @Mock
    private DiffSyncService diffSyncService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private MessageTypeBridgeConverter messageTypeBridgeConverter;
    @Mock
    private DiffSyncStartMessage message;
    @Mock
    private ServerIdentifier serverIdentifier;
    @Mock
    private Provider<DiffSyncSubscriptionManager> subscriptionManagerProvider;
    @Mock
    private DiffSyncSubscriptionManager diffSyncSubscriptionManager;
    @Mock
    private DiffSyncSubscription.DiffSyncSubscriptionFactory diffSyncSubscriptionFactory;
    @Mock
    private DiffSyncProducer diffSyncProducer;

    private DiffSyncEntity diffSyncEntity;

    private DiffSyncIdentifier diffSyncIdentifier;
    @Mock
    private DiffSyncGateway diffSyncGateway;

    private static final String clientId = "12345";
    private static final UUID entityId = UUID.randomUUID();
    private static final String entityName = "activityConfig";
    private static final String serverId = "aa-1234-de-34556";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getEntityType()).thenReturn(EntityType.ACTIVITY_CONFIG);
        when(message.getEntityId()).thenReturn(entityId);

        session = mockSession();

        diffSyncIdentifier = new DiffSyncIdentifier()
                .setType(DiffSyncIdentifierType.CLIENT)
                .setServerId(serverId)
                .setClientId(clientId);
        diffSyncEntity = new DiffSyncEntity().setEntityId(entityId).setEntityType(EntityType.ACTIVITY_CONFIG);
        RTMClientContext authenticationContext = mock(RTMClientContext.class);
        when(rtmClientContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getClientId()).thenReturn(clientId);
        when(serverIdentifier.getServerId()).thenReturn(serverId);

        DiffSyncSubscription diffSyncSubscription = new DiffSyncSubscription(diffSyncEntity, diffSyncIdentifier);
        when(diffSyncSubscriptionFactory.create(any(DiffSyncEntity.class), any(DiffSyncIdentifier.class))).thenReturn(
                diffSyncSubscription);
        when(subscriptionManagerProvider.get()).thenReturn(diffSyncSubscriptionManager);
        when(diffSyncSubscriptionManager.add(diffSyncSubscription)).thenReturn(Mono.just(1));


        handler = new DiffSyncStartMessageHandler(diffSyncService,
                                                  rtmClientContextProvider,
                                                  messageTypeBridgeConverter,
                                                  diffSyncSubscriptionFactory,
                                                  subscriptionManagerProvider,
                                                  diffSyncProducer,
                                                  serverIdentifier);
    }

    @Test
    void validate_noEntityName() {
        when(message.getEntityType()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing entity type", ex.getMessage());
    }

    @Test
    void validate_noEntityId() {
        when(message.getEntityId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing entityId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        RTMChannel channel = new RTMChannelFactory(messageTypeBridgeConverter).create(session, clientId);
        ServerShadow serverShadow = new ServerShadow(diffSyncIdentifier,
                                                     "",
                                                     new Version(),
                                                     new Version());
        ServerBackup serverBackup = new ServerBackup(diffSyncIdentifier, "", new Version());

        when(diffSyncService.start(diffSyncEntity,
                                   channel,
                                   diffSyncIdentifier))
                .thenReturn(Mono.just(new DiffSync(serverShadow,
                                                   serverBackup,
                                                   channel,
                                                   diffSyncEntity,
                                                   diffSyncIdentifier,
                                                   diffSyncGateway)));
        when(diffSyncProducer.buildConsumableMessage(any(Message.class), any(DiffSyncIdentifier.class), any(DiffSyncEntity.class)))
                .thenReturn(diffSyncProducer);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(DIFF_SYNC_START_OK, response.getType());
            });
        });
       verify(diffSyncService, atLeastOnce()).start(any(DiffSyncEntity.class),
                                                     any(Channel.class),
                                                     any(DiffSyncIdentifier.class));
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(diffSyncService.start(any(DiffSyncEntity.class),
                                   any(Channel.class),
                                   any(DiffSyncIdentifier.class)))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + DIFF_SYNC_START_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to diff sync start\"}");
    }
}
