package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionListMessageHandler.PROJECT_INGESTION_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionListMessageHandler.PROJECT_INGESTION_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummaryPayload;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionListMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ProjectIngestionListMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionListMessageHandler handler;
    @Mock
    private IngestionService ingestionService;

    private Session session;
    @Mock
    private ProjectIngestionListMessage message;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID accountId = UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";

    private final IngestionSummaryPayload ingestionSummary = new IngestionSummaryPayload()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setWorkspaceId(workspaceId)
            .setConfigFields(configFields)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setCreator(new AccountPayload().setAccountId(accountId))
            .setStatus(IngestionStatus.UPLOADING)
            .setIngestionStats(null);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(message.getProjectId()).thenReturn(projectId);
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing project id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(ingestionService.fetchIngestionsForProject(eq(projectId)))
                .thenReturn(Flux.just(ingestionSummary));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_LIST_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionSummaryPayload> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(ingestionService.fetchIngestionsForProject(eq(projectId)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + PROJECT_INGESTION_LIST_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error listing the ingestion summaries\"}");
    }
}
