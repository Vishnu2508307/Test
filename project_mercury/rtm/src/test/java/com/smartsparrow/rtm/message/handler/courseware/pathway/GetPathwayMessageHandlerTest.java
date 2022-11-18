package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.pathway.GetPathwayMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetPathwayMessageHandlerTest {

    @InjectMocks
    private GetPathwayMessageHandler handler;

    @Mock
    private PathwayService pathwayService;

    private GetPathwayMessage message;
    private static final String messageId = "123456";
    private static final UUID pathwayId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(GetPathwayMessage.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getPathwayId()).thenReturn(pathwayId);
    }

    @Test
    void validate_noPathwayId() {
        when(message.getPathwayId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("pathwayId is missing", ex.getMessage());
        assertEquals("BAD_REQUEST", ex.getType());
        assertEquals(400, ex.getResponseStatusCode());
    }

    @Test
    void validate_pathwayNotFound() {
        when(pathwayService.findById(message.getPathwayId())).thenReturn(Mono.empty());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("Pathway not found", t.getMessage());
        assertEquals("BAD_REQUEST", t.getType());
        assertEquals(400, t.getResponseStatusCode());

    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<PathwayPayload> publisher = TestPublisher.create();
        publisher.error(new PathwayNotFoundException(pathwayId));
        when(pathwayService.getPathwayPayload(pathwayId)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.pathway.get.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error while fetching pathway " + pathwayId + "\"," +
                            "\"replyTo\":\"123456\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        UUID parentActivity = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Pathway pathway = mockPathway(pathwayId);
        WalkableChild child = new WalkableChild()
                .setElementId(childId)
                .setElementType(CoursewareElementType.ACTIVITY);


        List<WalkableChild> children = Lists.newArrayList(child);

        PathwayPayload payload = PathwayPayload.from(pathway, parentActivity, children, null, new CoursewareElementDescription());

        when(pathwayService.getPathwayPayload(pathwayId)).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.pathway.get.ok\"," +
                            "\"response\":{" +
                                "\"pathway\":{" +
                                    "\"pathwayId\":\"" + pathwayId + "\"," +
                                    "\"pathwayType\":\"LINEAR\"," +
                                    "\"parentActivityId\":\"" + parentActivity + "\"," +
                                    "\"children\":[{" +
                                        "\"elementId\":\"" + childId + "\"," +
                                        "\"elementType\":\"ACTIVITY\"" +
                                    "}]" +
                            "}},\"replyTo\":\"123456\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
