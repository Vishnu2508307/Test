package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionLogListMessageHandler.PROJECT_INGESTION_LOG_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionLogListMessageHandler.PROJECT_INGESTION_LOG_LIST_OK;
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
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.ingestion.data.IngestionEvent;
import com.smartsparrow.ingestion.data.IngestionEventType;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ProjectIngestionLogListMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionLogListMessageHandler handler;
    @Mock
    private IngestionService ingestionService;
    private Session session;
    @Mock
    private IngestionGenericMessage message;


    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID accountId =  UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();

    private final IngestionEvent ingestionEvent = new IngestionEvent()
            .setEventType(IngestionEventType.ERROR)
            .setIngestionId(ingestionId)
            .setProjectId(projectId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(message.getIngestionId()).thenReturn(ingestionId);
    }

    @Test
    void validate_noIngestionId() {
        when(message.getIngestionId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing ingestion id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(ingestionService.fetchLogEventsForIngestion(eq(ingestionId)))
                .thenReturn(Flux.just(ingestionEvent));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_LOG_LIST_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionEvent> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(ingestionService.fetchLogEventsForIngestion(eq(ingestionId)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + PROJECT_INGESTION_LOG_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"error fetching the ingestion logs\"}");
    }
}
