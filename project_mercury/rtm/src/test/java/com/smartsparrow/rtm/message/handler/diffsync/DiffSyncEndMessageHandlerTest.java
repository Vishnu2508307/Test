package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncEndMessageHandler.DIFF_SYNC_END_ERROR;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncEndMessageHandler.DIFF_SYNC_END_OK;
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
import com.smartsparrow.cache.diffsync.DiffSyncSubscription;
import com.smartsparrow.cache.diffsync.DiffSyncSubscriptionManager;
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncEndMessage;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncMessage;

import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncService;
import data.EntityType;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DiffSyncEndMessageHandlerTest {
    private Session session;

    @InjectMocks
    DiffSyncEndMessageHandler handler;

    @Mock
    private DiffSyncService diffSyncService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private DiffSyncEndMessage message;
    @Mock
    private ServerIdentifier serverIdentifier;
    @Mock
    private Provider<DiffSyncSubscriptionManager> subscriptionManagerProvider;
    @Mock
    private DiffSyncSubscriptionManager diffSyncSubscriptionManager;
    @Mock
    private DiffSyncSubscription.DiffSyncSubscriptionFactory diffSyncSubscriptionFactory;
    @Mock
    private DiffSyncEntity diffSyncEntity;
    @Mock
    private DiffSyncIdentifier diffSyncIdentifier;

    private static final String clientId = "12345";
    private static final UUID entityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getEntityType()).thenReturn(EntityType.ACTIVITY_CONFIG);
        when(message.getEntityId()).thenReturn(entityId);

        session = mockSession();

        RTMClientContext authenticationContext = mock(RTMClientContext.class);
        when(rtmClientContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getClientId()).thenReturn(clientId);
        DiffSyncSubscription diffSyncSubscription = new DiffSyncSubscription(diffSyncEntity, diffSyncIdentifier);
        when(diffSyncSubscriptionFactory.create(any(DiffSyncEntity.class), any(DiffSyncIdentifier.class))).thenReturn(
                diffSyncSubscription);
        when(subscriptionManagerProvider.get()).thenReturn(diffSyncSubscriptionManager);

        handler = new DiffSyncEndMessageHandler(diffSyncService,
                                                rtmClientContextProvider,
                                                diffSyncSubscriptionFactory,
                                                subscriptionManagerProvider,
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

        when(diffSyncService.end(any(DiffSyncEntity.class)))
                .thenReturn(Mono.empty());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(DIFF_SYNC_END_OK, response.getType());
            });
        });
        verify(diffSyncService, atLeastOnce()).end(any(DiffSyncEntity.class));
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(diffSyncService.end(any(DiffSyncEntity.class)))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + DIFF_SYNC_END_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error ending the diff sync\"}");
    }
}
