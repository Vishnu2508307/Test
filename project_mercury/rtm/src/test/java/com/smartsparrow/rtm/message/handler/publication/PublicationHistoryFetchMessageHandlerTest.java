package com.smartsparrow.rtm.message.handler.publication;

import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.publication.data.PublicationPayload;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationHistoryFetchMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationHistoryFetchMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

public class PublicationHistoryFetchMessageHandlerTest {

    @InjectMocks
    private PublicationHistoryFetchMessageHandler handler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private PublicationHistoryFetchMessage message;

    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID publishedBy = UUID.randomUUID();
    private static final UUID updatedAt = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final PublicationOutputType publicationOutputType = PublicationOutputType.EPUB_ETEXT;
    private static final String etextVersion = "version01";

    private Session session;

    private PublicationPayload payload;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        payload = new PublicationPayload()
                .setPublicationId(publicationID)
                .setDescription("No description")
                .setAuthor("Test Author")
                .setTitle("Test Title")
                .setConfig("No config")
                .setPublishedBy(publishedBy)
                .setUpdatedAt(updatedAt)
                .setActivityId(activityId)
                .setEtextVersion(etextVersion);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(message.getActivityId()).thenReturn(activityId);
        when(publicationService.fetchPublicationForActivity(activityId, null)).thenReturn(Flux.just(payload));
        handler.handle(session, message);
        String expectedMessage = "{\"type\":\"publication.history.fetch.ok\"," +
                "\"response\":{\"publications\":[{" +
                "\"publicationId\":\""+publicationID+"\"," +
                "\"title\":\"Test Title\"," +
                "\"description\":\"No description\"," +
                "\"config\":\"No config\"," +
                "\"author\":\"Test Author\"," +
                "\"publishedBy\":\""+publishedBy+"\"," +
                "\"updatedAt\":\""+updatedAt+"\"," +
                "\"activityId\":\""+activityId+"\"," +
                "\"etextVersion\":\""+etextVersion+"\"}]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void handle_success_output_type() throws WriteResponseException {
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getOutputType()).thenReturn(publicationOutputType);
        when(publicationService.fetchPublicationForActivity(activityId, publicationOutputType)).thenReturn(Flux.just(payload));
        handler.handle(session, message);
        String expectedMessage = "{\"type\":\"publication.history.fetch.ok\"," +
                "\"response\":{\"publications\":[{" +
                "\"publicationId\":\""+publicationID+"\"," +
                "\"title\":\"Test Title\"," +
                "\"description\":\"No description\"," +
                "\"config\":\"No config\"," +
                "\"author\":\"Test Author\"," +
                "\"publishedBy\":\""+publishedBy+"\"," +
                "\"updatedAt\":\""+updatedAt+"\"," +
                "\"activityId\":\""+activityId+"\"," +
                "\"etextVersion\":\""+etextVersion+"\"}]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void handle_failure() throws WriteResponseException {
        TestPublisher<PublicationPayload> publications = TestPublisher.create();
        publications.error(new RuntimeException("@#$%*!!"));

        when(publicationService.fetchPublicationWithMeta()).thenReturn(publications.flux());

        handler.handle(session, message);

        String expectedMessage = "{\"type\":\"publication.history.fetch.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetching publications\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }
}
