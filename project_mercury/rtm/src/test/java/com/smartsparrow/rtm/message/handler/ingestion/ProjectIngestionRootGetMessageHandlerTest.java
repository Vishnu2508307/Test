package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionRootGetMessageHandler.PROJECT_INGESTION_ROOT_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.ingestion.ProjectIngestionRootGetMessageHandler.PROJECT_INGESTION_ROOT_GET_OK;
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
import com.smartsparrow.rtm.message.recv.ingestion.IngestionRootGenericMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ProjectIngestionRootGetMessageHandlerTest {

    @InjectMocks
    private ProjectIngestionRootGetMessageHandler handler;

    @Mock
    private IngestionService ingestionService;
    private Session session;
    @Mock
    private IngestionRootGenericMessage message;
    private IngestionSummaryPayload summary;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID accountId =  UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(message.getRootElementId()).thenReturn(rootElementId);

        summary = new IngestionSummaryPayload()
                .setId(ingestionId)
                .setProjectId(projectId)
                .setConfigFields(configFields)
                .setAmbrosiaUrl(ambrosiaUrl)
                .setCreator(new AccountPayload().setAccountId(accountId))
                .setStatus(IngestionStatus.UPLOADING)
                .setIngestionStats(null)
                .setRootElementId(rootElementId);
    }

    @Test
    void validate_noRootElementId() {
        when(message.getRootElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing root element id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(ingestionService.fetchIngestionForProjectByRootElement(eq(rootElementId)))
                .thenReturn(Flux.just(summary));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(PROJECT_INGESTION_ROOT_GET_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IngestionSummaryPayload> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(ingestionService.fetchIngestionForProjectByRootElement(eq(rootElementId)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + PROJECT_INGESTION_ROOT_GET_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error fetching the ingestionRequest by root element\"}");
    }

}
