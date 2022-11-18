package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionGetMessageHandler.PROJECT_INGESTION_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionGetMessageHandler.PROJECT_INGESTION_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummaryPayload;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class ProjectIngestionGetMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionGetMessageHandler handler;
    @Mock
    private IngestionService ingestionService;
    private Session session;
    @Mock
    private IngestionGenericMessage message;
    private IngestionSummaryPayload summary;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID accountId =  UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        when(message.getIngestionId()).thenReturn(ingestionId);

        summary = new IngestionSummaryPayload()
                          .setId(ingestionId)
                          .setProjectId(projectId)
                          .setConfigFields(configFields)
                          .setAmbrosiaUrl(ambrosiaUrl)
                          .setCreator(new AccountPayload().setAccountId(accountId))
                          .setStatus(IngestionStatus.UPLOADING)
                          .setIngestionStats(null);
    }

    @Test
    void validate_noIngestionId() {
        when(message.getIngestionId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing ingestion id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(ingestionService.getIngestionPayload(eq(ingestionId)))
                .thenReturn(Mono.just(summary));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_GET_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionSummaryPayload> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch"));
        when(ingestionService.getIngestionPayload(eq(ingestionId)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + PROJECT_INGESTION_GET_ERROR + "\",\"code\":422," +
                "\"message\":\"error fetching the ingestionRequest\"}");
    }
}
