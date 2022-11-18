package com.smartsparrow.rtm.message.handler.publication;

import com.smartsparrow.publication.data.PublicationPayload;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.ListPublicationRequestMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationListMessage;
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

public class ListPublicationRequestMessageHandlerTest {

    @InjectMocks
    private ListPublicationRequestMessageHandler listPublicationRequestMessageHandler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private PublicationListMessage publicationListMessage;

    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID publishedBy = UUID.randomUUID();
    private static final UUID updatedAt = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final String etextVersion = "version01";

    private Session session;

    private PublicationPayload publicationPayload;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        publicationPayload = new PublicationPayload()
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
        when(publicationService.fetchPublicationWithMeta()).thenReturn(Flux.just(publicationPayload));
        listPublicationRequestMessageHandler.handle(session, publicationListMessage);
        String expectedMessage = "{\"type\":\"publication.list.request.ok\"," +
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

        listPublicationRequestMessageHandler.handle(session, publicationListMessage);

        String expectedMessage = "{\"type\":\"publication.list.request.error\"," +
                "\"code\":422," +
                "\"message\":\"error listing publications\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }
}
