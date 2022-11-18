package com.smartsparrow.rtm.message.handler.publication;

import com.smartsparrow.publication.data.PublicationActivityPayload;
import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.publication.data.PublicationPayload;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationActivityFetchMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationActivityFetchMessage;
import com.smartsparrow.workspace.service.ProjectService;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.util.UUID;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class PublicationActivityFetchMessageHandlerTest {

    @InjectMocks
    private PublicationActivityFetchMessageHandler handler;

    @Mock
    private PublicationService publicationService;

    @Mock
    private ProjectService projectService;

    @Mock
    private PublicationActivityFetchMessage message;

    private static final UUID publicationID = UUID.randomUUID();
    private static final UUID publishedBy = UUID.randomUUID();
    private static final UUID updatedAt = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final String etextVersion = "version01";
    private static final UUID workspaceId = UUID.randomUUID();
    private static final PublicationOutputType outputType = PublicationOutputType.EPUB_ETEXT;

    private Session session;

    private PublicationActivityPayload payload;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        payload = new PublicationActivityPayload()
                .setAuthor("Test Author")
                .setTitle("Test Title")
                .setPublishedBy(publishedBy)
                .setUpdatedAt(updatedAt)
                .setActivityId(activityId)
                .setOutputType(outputType);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(projectService.findWorkspaceActivities(workspaceId)).thenReturn(Flux.just(activityId));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(publicationService.fetchPublishedActivities()).thenReturn(Flux.just(payload));
        handler.handle(session, message);
        String expectedMessage = "{\"type\":\"publication.activity.fetch.ok\"," +
                "\"response\":{\"activities\":[{" +
                "\"title\":\"Test Title\"," +
                "\"author\":\"Test Author\"," +
                "\"publishedBy\":\""+publishedBy+"\"," +
                "\"updatedAt\":\""+updatedAt+"\"," +
                "\"activityId\":\""+activityId+"\"," +
                "\"outputType\":\""+outputType+"\"}]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void handle_failure() throws WriteResponseException {
        TestPublisher<PublicationActivityPayload> activities = TestPublisher.create();
        activities.error(new RuntimeException("@#$%*!!"));

        when(publicationService.fetchPublishedActivities()).thenReturn(activities.flux());

        handler.handle(session, message);

        String expectedMessage = "{\"type\":\"publication.activity.fetch.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetch activities\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }
}
