package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.CreateInteractiveFeedbackMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_CREATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.payload.FeedbackPayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.feedback.CreateInteractiveFeedbackMessage;
import com.smartsparrow.rtm.subscription.courseware.created.FeedbackCreatedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CreateInteractiveFeedbackMessageHandlerTest {

    @InjectMocks
    private CreateInteractiveFeedbackMessageHandler createInteractiveFeedbackMessageHandler;

    private CreateInteractiveFeedbackMessage message;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private FeedbackCreatedRTMProducer feedbackCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID feedbackId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.2.3";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        message = mock(CreateInteractiveFeedbackMessage.class);
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExp()).thenReturn(pluginVersion);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(coursewareService.getRootElementId(interactiveId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                                                                    .setId(UUID.randomUUID()));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(feedbackCreatedRTMProducer.buildFeedbackCreatedRTMConsumable(rtmClientContext, rootElementId, feedbackId))
                .thenReturn(feedbackCreatedRTMProducer);

        createInteractiveFeedbackMessageHandler = new CreateInteractiveFeedbackMessageHandler(feedbackService,
                                                                                              coursewareService,
                                                                                              rtmEventBrokerProvider,
                                                                                              authenticationContextProvider,
                                                                                              rtmClientContextProvider,
                                                                                              feedbackCreatedRTMProducer);
    }

    @Test
    void validateMissingInteractiveId() {
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        createInteractiveFeedbackMessageHandler.validate(new CreateInteractiveFeedbackMessage()));
        assertEquals("interactiveId is required", response.getErrorMessage());
    }

    @Test
    void validateMissingPluginId() {
        CreateInteractiveFeedbackMessage m = mock(CreateInteractiveFeedbackMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);

        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        createInteractiveFeedbackMessageHandler.validate(m));
        assertEquals("plugin id is required", response.getErrorMessage());
    }

    @Test
    void validateMissingPluginVersionExpr() {
        CreateInteractiveFeedbackMessage m = mock(CreateInteractiveFeedbackMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        when(m.getPluginId()).thenReturn(pluginId);

        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        createInteractiveFeedbackMessageHandler.validate(m));
        assertEquals("plugin version is required", response.getErrorMessage());
    }

    @Test
    void validateSuccess() throws RTMValidationException {
        CreateInteractiveFeedbackMessage m = mock(CreateInteractiveFeedbackMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        when(m.getPluginId()).thenReturn(pluginId);
        when(m.getPluginVersionExp()).thenReturn(pluginVersion);
        createInteractiveFeedbackMessageHandler.validate(m);
    }

    @Test
    void handle() throws WriteResponseException, VersionParserFault {
        Feedback feedback = new Feedback()
                .setId(feedbackId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion);

        PluginSummary plugin = new PluginSummary()
                .setId(pluginId)
                .setName("pluginName")
                .setType(PluginType.COURSE);

        FeedbackPayload payload = FeedbackPayload.from(feedback, plugin, interactiveId, "", new ArrayList<>());

        when(feedbackService.create(any(), any(), any())).thenReturn(Mono.just(feedback));
        when(feedbackService.getFeedbackPayload(eq(feedback))).thenReturn(Mono.just(payload));

        createInteractiveFeedbackMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture("{\"type\":\"author.interactive.feedback.create.ok\"," +
                "\"response\":" +
                "{\"feedback\":" +
                "{\"feedbackId\":\"" + feedbackId + "\"," +
                "\"interactiveId\":\"" + interactiveId + "\"," +
                "\"plugin\":{" +
                "\"pluginId\":\"" + pluginId + "\"," +
                "\"name\":\"pluginName\"," +
                "\"type\":\"course\"," +
                "\"version\":\"" + pluginVersion + "\"}}}}");
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_FEEDBACK_CREATE), captor.capture());
        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(feedbackId, captor.getValue().getElement().getElementId());
        assertEquals(CoursewareElementType.FEEDBACK, captor.getValue().getElement().getElementType());
        assertEquals(interactiveId, captor.getValue().getParentElement().getElementId());
        assertEquals(INTERACTIVE, captor.getValue().getParentElement().getElementType());

        verify(feedbackCreatedRTMProducer, atLeastOnce()).buildFeedbackCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(feedbackId));
        verify(feedbackCreatedRTMProducer, atLeastOnce()).produce();
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_PluginNotFound() throws WriteResponseException, VersionParserFault {
        Mono mono = TestPublisher.create().error(new PluginNotFoundFault("Plugin not found")).mono();
        when(feedbackService.create(any(), any(), any())).thenReturn(mono);

        createInteractiveFeedbackMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture("{\"type\":\"author.interactive.feedback.create.error\"," +
                "\"code\":400," +
                "\"message\":\"Plugin not found\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(feedbackCreatedRTMProducer, never()).produce();
    }
}
