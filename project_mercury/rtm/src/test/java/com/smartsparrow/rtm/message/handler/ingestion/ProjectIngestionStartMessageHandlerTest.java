package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.ingestion.data.IngestionStatus.UPLOADED;
import static com.smartsparrow.ingestion.data.IngestionAdapterType.EPUB;
import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionStartMessageHandler.PROJECT_INGESTION_START_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionStartMessageHandler.PROJECT_INGESTION_START_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.WebToken;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionStartMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ProjectIngestionStartMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionStartMessageHandler handler;
    @Mock
    private IngestionService ingestionService;
    private Session session;
    @Mock
    private ProjectIngestionStartMessage message;
    @Mock
    private IngestionSummary summary;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private WebToken webToken;

    private static final UUID ingestionId = UUID.randomUUID();
        private static final String bearerToken = "iENrnPXyR3u09i4lxKVujOnyhOlxx1aZ";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
                when(authenticationContext.getWebToken()).thenReturn(webToken);
        when(webToken.getToken()).thenReturn(bearerToken);
        when(message.getIngestionId()).thenReturn(ingestionId);
    }

    @Test
    void validate_noIngestionId() {
        when(message.getIngestionId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing ingestion id", ex.getMessage());
    }

    @Test
    void validate_noAdapter() {
        when(message.getAdapterType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing adapter type", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(summary.getStatus()).thenReturn(UPLOADED);
        when(message.getAdapterType()).thenReturn(EPUB);
        when(ingestionService.findById(eq(ingestionId))).thenReturn(Mono.just(summary));
        when(ingestionService.publishToSQS(eq(summary), eq(SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST), eq(bearerToken)))
                .thenReturn(Mono.just(summary));

        handler.handle(session, message);

        verify(ingestionService).publishToSQS(eq(summary), eq(SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST), eq(bearerToken));
        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_START_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionSummary> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(ingestionService.findById(eq(ingestionId))).thenReturn(error.mono());

        handler.handle(session, message);

        verify(ingestionService, never()).publishToSQS(eq(summary), eq(SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST), eq(bearerToken));
        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + PROJECT_INGESTION_START_ERROR + "\",\"code\":422," +
                "\"message\":\"error starting the ingestionRequest\"}");
    }
}
