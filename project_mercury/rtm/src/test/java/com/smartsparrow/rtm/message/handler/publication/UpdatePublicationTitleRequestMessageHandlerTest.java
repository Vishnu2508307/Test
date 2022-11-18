package com.smartsparrow.rtm.message.handler.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.publication.data.PublicationPayload;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.UpdatePublicationTitleRequestMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.UpdatePublicationTitleMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class UpdatePublicationTitleRequestMessageHandlerTest {

    @InjectMocks
    private UpdatePublicationTitleRequestMessageHandler updatePublicationTitleRequestMessageHandler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private UpdatePublicationTitleMessage updatePublicationTitleMessage;

    private static final UUID activityId = UUID.randomUUID();
    private static final String title = "Update Title";
    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID publishedBy = UUID.randomUUID();
    private static final UUID updatedAt = UUID.randomUUID();
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
        when(updatePublicationTitleMessage.getActivityId()).thenReturn(activityId);
        when(updatePublicationTitleMessage.getTitle()).thenReturn(title);
        when(updatePublicationTitleMessage.getVersion()).thenReturn(etextVersion);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(publicationService.updateTitle(activityId, title, etextVersion)).thenReturn(Mono.empty());
        when(publicationService.fetchPublicationForActivity(activityId)).thenReturn(Flux.just(payload));
        updatePublicationTitleRequestMessageHandler.handle(session, updatePublicationTitleMessage);

        String expectedMessage = "{\"type\":\"publication.title.update.request.ok\"," +
                "\"response\":{\"publicationPayload\":[{" +
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
    void handle_exception() throws WriteResponseException {
        TestPublisher publication = TestPublisher.create();
        publication.error(new RuntimeException("error updating title of publication"));
        when(publicationService.updateTitle(activityId, title, etextVersion)).thenReturn(publication.mono());
        when(publicationService.fetchPublicationForActivity(activityId)).thenReturn(Flux.just(payload));
        updatePublicationTitleRequestMessageHandler.handle(session, updatePublicationTitleMessage);
        String expectedMessage = "{\"type\":\"publication.title.update.request.error\"," +
                "\"code\":422," +
                "\"message\":\"error updating title of publication\"}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void validate_noActivityId() {
        when(updatePublicationTitleMessage.getActivityId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> updatePublicationTitleRequestMessageHandler.validate(
                                                       updatePublicationTitleMessage));
        assertEquals("missing activityId", ex.getMessage());
    }

    @Test
    void validate_noTitle() {
        when(updatePublicationTitleMessage.getTitle()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> updatePublicationTitleRequestMessageHandler.validate(
                                                       updatePublicationTitleMessage));
        assertEquals("missing title", ex.getMessage());
    }

    @Test
    void validate_noVersion() {
        when(updatePublicationTitleMessage.getVersion()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                                               () -> updatePublicationTitleRequestMessageHandler.validate(
                                                       updatePublicationTitleMessage));
        assertEquals("missing version", ex.getMessage());
    }

}
