package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.GetInteractiveMessageHandler.AUTHOR_INTERACTIVE_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.GetInteractiveMessageHandler.AUTHOR_INTERACTIVE_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementDescription;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.interactive.GetInteractiveMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetInteractiveMessageHandlerTest {

    @InjectMocks
    private GetInteractiveMessageHandler handler;

    @Mock
    private GetInteractiveMessage message;

    @Mock
    private InteractiveService interactiveService;

    private Session session;
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final String messageId = "message id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(interactiveService.findParentPathwayId(eq(interactiveId))).thenReturn(Mono.just(interactiveId));
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getId()).thenReturn(messageId);

    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("interactiveId is missing", ex.getMessage());
        assertEquals("BAD_REQUEST", ex.getType());
        assertEquals(400, ex.getResponseStatusCode());
    }

    @Test
    void validate_interactiveNotFound() {
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(Mono.empty());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("Interactive not found", t.getMessage());
        assertEquals("BAD_REQUEST", t.getType());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle() throws IOException {
        InteractivePayload payload = InteractivePayload.from(new Interactive().setId(interactiveId),
                new PluginSummary(),
                new InteractiveConfig(),
                parentPathwayId,
                Lists.newArrayList(),
                Lists.newArrayList(),
                new CoursewareElementDescription(), new ArrayList<>());
        when(interactiveService.getInteractivePayload(eq(interactiveId))).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_INTERACTIVE_GET_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("interactive"));
                assertEquals(interactiveId.toString(), responseMap.get("interactiveId"));
                assertEquals(parentPathwayId.toString(), responseMap.get("parentPathwayId"));
            });
        });
    }

    @Test
    void handle_error_noInteractive() {
        TestPublisher<InteractivePayload> publisher = TestPublisher.create();
        publisher.error(new InteractiveNotFoundException(interactiveId));
        when(interactiveService.getInteractivePayload(eq(interactiveId))).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_GET_ERROR + "\",\"code\":404," +
                "\"message\":\"no interactive with id " + interactiveId + "\",\"replyTo\":\"message id\"}");
    }

    @Test
    void handle_error_unprocessableEntity() {
        TestPublisher<InteractivePayload> publisher = TestPublisher.create();
        publisher.error(new ParentPathwayNotFoundException(interactiveId));
        when(interactiveService.getInteractivePayload(eq(interactiveId))).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_GET_ERROR + "\",\"code\":422," +
                "\"message\":\"parent pathway not found for interactive " + interactiveId + "\","
                + "\"replyTo\":\"message id\"}");
    }
}
