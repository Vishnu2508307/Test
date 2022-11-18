package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncPatchMessageHandler.DIFF_SYNC_PATCH_ERROR;
import static com.smartsparrow.rtm.message.handler.diffsync.DiffSyncPatchMessageHandler.DIFF_SYNC_PATCH_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncPatchMessage;

import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncService;
import data.EntityType;
import data.Message;
import data.Patch;
import data.PatchRequest;
import data.Version;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DiffSyncPatchMessageHandlerTest {
    private Session session;

    @InjectMocks
    DiffSyncPatchMessageHandler handler;

    @Mock
    private DiffSyncService diffSyncService;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private DiffSyncPatchMessage message;
    @Mock
    private ServerIdentifier serverIdentifier;
    @Mock
    private DiffSyncProducer diffSyncProducer;


    private static final String clientId = "12345";
    private static final UUID patchId = UUID.randomUUID();
    private static final UUID entityId = UUID.randomUUID();
    private List<Patch> patches;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        patches = Arrays.asList(new Patch()
                                                    .setClientId(clientId)
                                                    .setId(patchId)
                                                    .setM(new Version().setValue(Long.valueOf(1)))
                                                    .setN(new Version().setValue(Long.valueOf(1))));
        when(message.getEntityType()).thenReturn(EntityType.ACTIVITY_CONFIG);
        when(message.getPatches()).thenReturn(patches);
        when(message.getEntityId()).thenReturn(entityId);

        session = mockSession();

        RTMClientContext authenticationContext = mock(RTMClientContext.class);
        when(rtmClientContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getClientId()).thenReturn(clientId);
        when(diffSyncProducer.buildConsumableMessage(any(Message.class), any(DiffSyncIdentifier.class), any(DiffSyncEntity.class)))
                .thenReturn(diffSyncProducer);

        handler = new DiffSyncPatchMessageHandler(diffSyncService, rtmClientContextProvider, serverIdentifier, diffSyncProducer);
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
    void validate_emptyOrNullPatches() {
        when(message.getPatches()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing patches", ex.getMessage());
    }

    @Test
    void handle() throws IOException {

        when(diffSyncService.syncPatch(any(DiffSyncEntity.class), any(List.class), any(DiffSyncIdentifier.class)))
                .thenReturn(Mono.just(new Patch()));


        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(DIFF_SYNC_PATCH_OK, response.getType());
            });
        });
        verify(diffSyncService, atLeastOnce()).syncPatch(any(DiffSyncEntity.class), any(List.class), any(DiffSyncIdentifier.class));
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(diffSyncService.syncPatch(any(DiffSyncEntity.class), any(List.class), any(DiffSyncIdentifier.class)))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + DIFF_SYNC_PATCH_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to diff sync patch\"}");
    }
}
