package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncAckMessageHandler.DIFF_SYNC_ACK_OK;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncAckMessageHandler.DIFF_SYNC_ACK_ERROR;
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
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncAckMessage;

import data.Ack;
import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncService;
import data.EntityType;
import data.Message;
import data.Version;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DiffSyncAckMessageHandlerTest {
    private Session session;

    @InjectMocks
    DiffSyncAckMessageHandler handler;

    @Mock
    private DiffSyncService diffSyncService;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private DiffSyncAckMessage message;
    @Mock
    private DiffSyncEntity diffSyncEntity;
    @Mock
    private ServerIdentifier serverIdentifier;
    @Mock
    private DiffSyncProducer diffSyncProducer;

    private static final String clientId = "12345";
    private static final UUID entityId = UUID.randomUUID();
    private Version nVersion;
    private Version mVersion;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        nVersion = new Version().setValue(Long.valueOf(1));
        mVersion = new Version().setValue(Long.valueOf(1));

        when(message.getEntityType()).thenReturn(EntityType.ACTIVITY_CONFIG);
        when(message.getM()).thenReturn(mVersion);
        when(message.getN()).thenReturn(nVersion);
        when(message.getEntityId()).thenReturn(entityId);


        session = mockSession();

        RTMClientContext authenticationContext = mock(RTMClientContext.class);
        when(rtmClientContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getClientId()).thenReturn(clientId);
        when(diffSyncProducer.buildConsumableMessage(any(Message.class), any(DiffSyncIdentifier.class), any(DiffSyncEntity.class)))
                .thenReturn(diffSyncProducer);

        handler = new DiffSyncAckMessageHandler(diffSyncService, rtmClientContextProvider, serverIdentifier, diffSyncProducer);
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
    void validate_noMversion() {
        when(message.getM()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing m version", ex.getMessage());
    }

    @Test
    void validate_noNversion() {
        when(message.getN()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing n version", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(diffSyncService.syncAck(any(DiffSyncEntity.class), any(DiffSyncIdentifier.class), any(Ack.class)))
                .thenReturn(Mono.just(new Ack()));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(DIFF_SYNC_ACK_OK, response.getType());
            });
        });
        verify(diffSyncService, atLeastOnce()).syncAck(any(DiffSyncEntity.class), any(DiffSyncIdentifier.class), any(Ack.class));
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(diffSyncService.syncAck(any(DiffSyncEntity.class), any(DiffSyncIdentifier.class), any(Ack.class) ))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + DIFF_SYNC_ACK_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to diff sync ack\"}");
    }
}
