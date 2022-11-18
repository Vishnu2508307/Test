package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionCreateMessageHandler.PROJECT_INGESTION_CREATE;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionCreateMessageHandler.PROJECT_INGESTION_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionCreateMessageHandler.PROJECT_INGESTION_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.ingestion.data.IngestionPayload;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionCreateMessage;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class ProjectIngestionCreateMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionCreateMessageHandler handler;
    @Mock
    private IngestionService ingestionService;
    private Session session;
    @Mock
    private ProjectIngestionCreateMessage message;
    @Mock
    private IngestionSummary summary;
    @Mock
    private IngestionPayload payload;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
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

    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID accountId =  UUID.randomUUID();
    private static final String courseName = "Test Course";
    private static final String fileName = "content.epub";
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getConfigFields()).thenReturn(configFields);
        when(message.getIngestStats()).thenReturn(null);
        when(message.getCourseName()).thenReturn(courseName);
        when(message.getFileName()).thenReturn(fileName);

        when(summary.getId()).thenReturn(ingestionId);
        when(message.getRootElementId()).thenReturn(rootElementId);

        handler = new ProjectIngestionCreateMessageHandler(
                authenticationContextProvider,
                ingestionService,
                rtmEventBrokerProvider,
                rtmClientContextProvider, activityIngestionRTMProducer);

    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing project id", ex.getMessage());
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing workspace id", ex.getMessage());
    }

    @Test
    void validate_noConfigFields() {
        when(message.getConfigFields()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing configFields", ex.getMessage());
    }

    @Test
    void validate_noCourseName() {
        when(message.getCourseName()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing courseName", ex.getMessage());
    }


    @Test
    void validate_noFileName() {
        when(message.getFileName()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing fileName", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(ingestionService.create(eq(projectId), eq(workspaceId), eq(configFields), eq(accountId), eq(null), eq(null), eq(courseName), eq(rootElementId)))
                .thenReturn(Mono.just(summary));
        when(payload.getIngestionId()).thenReturn(ingestionId);
        when(ingestionService.createSignedUrl(eq(ingestionId), eq(fileName)))
                .thenReturn(Mono.just(payload));
        when(activityIngestionRTMProducer.buildIngestionConsumable(payload.getIngestionId(),
                                                                            message.getProjectId(),
                                                                              message.getRootElementId(),
                                                                              IngestionStatus.UPLOADING))
                .thenReturn(activityIngestionRTMProducer);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_CREATE_OK, response.getType());
            });
        });

        ArgumentCaptor<ProjectBroadcastMessage> captor = ArgumentCaptor.forClass(ProjectBroadcastMessage.class);

        verify(rtmEventBroker).broadcast(eq(PROJECT_INGESTION_CREATE), captor.capture());
        final ProjectBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(projectId, broadcastMessage.getProjectId());
        assertEquals(ingestionId, broadcastMessage.getIngestionId());
        assertEquals(IngestionStatus.UPLOADING, broadcastMessage.getIngestionStatus());
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionSummary> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(ingestionService.create(eq(projectId), eq(workspaceId), eq(configFields), eq(accountId), eq(null), eq(null), eq(courseName), eq(rootElementId)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + PROJECT_INGESTION_CREATE_ERROR + "\",\"code\":422," +
                "\"message\":\"error creating the ingestionSummary\"}");
    }

    @Test
    void handle_conflict_exception() throws IOException {
        TestPublisher<IngestionSummary> error = TestPublisher.create();
        error.error(new ConflictFault("course already exists"));
        when(ingestionService.create(eq(projectId), eq(workspaceId), eq(configFields), eq(accountId), eq(null), eq(null), eq(courseName), eq(rootElementId)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + PROJECT_INGESTION_CREATE_ERROR + "\",\"code\":409," +
                "\"message\":\"error creating the ingestionSummary. Course name already exists\"}");
    }
}
