package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_CANCEL_REQUEST;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionDeleteMessageHandler.PROJECT_INGESTION_DELETE;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionDeleteMessageHandler.PROJECT_INGESTION_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionDeleteMessageHandler.PROJECT_INGESTION_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionUpdateMessage;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ProjectIngestionDeleteMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionDeleteMessageHandler handler;
    @Mock
    private IngestionService ingestionService;
    private Session session;
    @Mock
    private IngestionUpdateMessage message;
    @Mock
    private IngestionSummary summary;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private ActivityIngestionProducer activityIngestionRTMProducer;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();
    private static final IngestionStatus status = IngestionStatus.UPLOAD_FAILED;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        when(message.getIngestionId()).thenReturn(ingestionId);

        when(summary.getProjectId()).thenReturn(projectId);
        when(summary.getId()).thenReturn(ingestionId);
        when(summary.getStatus()).thenReturn(status);
        handler = new ProjectIngestionDeleteMessageHandler(ingestionService, rtmClientContextProvider, rtmEventBrokerProvider, activityIngestionRTMProducer);
    }

    @Test
    void validate_noIngestionId() {
        when(message.getIngestionId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing ingestion id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(ingestionService.deleteById(eq(ingestionId))).thenReturn(Mono.just(summary));
        when(ingestionService.publishToSQS(eq(summary), eq(SUBMIT_INGESTION_CANCEL_REQUEST), eq("")))
                .thenReturn(Mono.just(summary));

        when(activityIngestionRTMProducer.buildIngestionConsumable(message.getIngestionId(),
                                                                            summary.getProjectId(),
                                                                              summary.getRootElementId(),
                                                                              IngestionStatus.DELETED))
                .thenReturn(activityIngestionRTMProducer);

        handler.handle(session, message);

        verify(ingestionService).publishToSQS(eq(summary), eq(SUBMIT_INGESTION_CANCEL_REQUEST), eq(""));

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_DELETE_OK, response.getType());
            });
        });

        ArgumentCaptor<ProjectBroadcastMessage> captor = ArgumentCaptor.forClass(ProjectBroadcastMessage.class);

        verify(rtmEventBroker).broadcast(eq(PROJECT_INGESTION_DELETE), captor.capture());
        final ProjectBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(projectId, broadcastMessage.getProjectId());
        assertEquals(ingestionId, broadcastMessage.getIngestionId());
        assertEquals(IngestionStatus.DELETED, broadcastMessage.getIngestionStatus());
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionSummary> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(ingestionService.deleteById(eq(ingestionId))).thenReturn(error.mono());

        handler.handle(session, message);

        verify(ingestionService, never()).publishToSQS(eq(summary), eq(SUBMIT_INGESTION_CANCEL_REQUEST), eq(""));
        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + PROJECT_INGESTION_DELETE_ERROR + "\",\"code\":422," +
                "\"message\":\"error deleting the ingestionRequest\"}");
    }
}
