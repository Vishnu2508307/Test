package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.GetInteractiveFeedbackMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.GetInteractiveFeedbackMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_GET_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.payload.FeedbackPayload;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.courseware.feedback.GetInteractiveFeedbackMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetInteractiveFeedbackMessageHandlerTest {

    @InjectMocks
    private GetInteractiveFeedbackMessageHandler handler;
    @Mock
    private FeedbackService feedbackService;
    @Mock
    private GetInteractiveFeedbackMessage message;
    private Session session;

    private static final UUID feedbackId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getFeedbackId()).thenReturn(feedbackId);
    }

    @Test
    void validate_noFeedbackId() {
        when(message.getFeedbackId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("feedbackId is required", t.getErrorMessage());
        assertEquals(400, t.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_GET_ERROR, t.getType());
    }

    @Test
    void validate() throws RTMValidationException {
        handler.validate(message);
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle() throws IOException {
        UUID pluginId = UUID.randomUUID();
        UUID interactiveId = UUID.randomUUID();
        FeedbackPayload payload = FeedbackPayload.from(
                new Feedback().setId(feedbackId).setPluginId(pluginId).setPluginVersionExpr("1.2.3"),
                new PluginSummary().setId(pluginId).setName("Test plugin").setType(PluginType.COMPONENT),
                interactiveId,
                "TestConfig",
                new ArrayList<>());

        when(feedbackService.getFeedbackPayload(feedbackId)).thenReturn(Mono.just(payload));
        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_GET_OK, response.getType());
            Map<String, Object> feedback = (Map<String, Object>)response.getResponse().get("feedback");
            assertEquals(4, feedback.entrySet().size());
            assertEquals(feedbackId.toString(), feedback.get("feedbackId"));
            assertEquals(interactiveId.toString(), feedback.get("interactiveId"));
            assertEquals("TestConfig", feedback.get("config"));
            Map<String, Object> plugin = (Map<String, Object>)feedback.get("plugin");
            assertEquals(pluginId.toString(), plugin.get("pluginId"));
            assertEquals("Test plugin", plugin.get("name"));
            assertEquals("component", plugin.get("type"));
            assertEquals("1.2.3", plugin.get("version"));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_emptyConfig() throws IOException {
        UUID pluginId = UUID.randomUUID();
        UUID interactiveId = UUID.randomUUID();
        FeedbackPayload payload = FeedbackPayload.from(
                new Feedback().setId(feedbackId).setPluginId(pluginId).setPluginVersionExpr("1.2.3"),
                new PluginSummary().setId(pluginId).setName("Test plugin").setType(PluginType.COMPONENT),
                interactiveId,
                "",
                new ArrayList<>());

        when(feedbackService.getFeedbackPayload(feedbackId)).thenReturn(Mono.just(payload));
        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_GET_OK, response.getType());
            Map<String, Object> feedback = (Map<String, Object>)response.getResponse().get("feedback");
            assertEquals(3, feedback.entrySet().size());
            assertNull(feedback.get("config"));
        });
    }

    @Test
    void handle_error() {
        TestPublisher<FeedbackPayload> publisher = TestPublisher.create();
        publisher.error(new FeedbackNotFoundException(feedbackId));
        when(feedbackService.getFeedbackPayload(feedbackId)).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_FEEDBACK_GET_ERROR + "\",\"code\":422," +
                "\"message\":\"no feedback with id " + feedbackId + "\"}");
    }
}
