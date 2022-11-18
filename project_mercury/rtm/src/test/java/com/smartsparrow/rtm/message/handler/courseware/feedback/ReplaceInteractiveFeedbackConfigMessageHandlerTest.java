package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.ReplaceInteractiveFeedbackConfigMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_REPLACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.FeedbackConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.feedback.ReplaceInteractiveFeedbackConfigMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.FeedbackConfigChangeRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class ReplaceInteractiveFeedbackConfigMessageHandlerTest {

    @InjectMocks
    ReplaceInteractiveFeedbackConfigMessageHandler replaceInteractiveFeedbackConfigMessageHandler;

    private ReplaceInteractiveFeedbackConfigMessage message;

    @Mock
    FeedbackService feedbackService;

    @Mock
    CoursewareService coursewareService;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private FeedbackConfigChangeRTMProducer feedbackConfigChangeRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID feedbackConfigId = UUID.randomUUID();
    private static final UUID feedbackId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final String config = "{someConfig}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        message = mock(ReplaceInteractiveFeedbackConfigMessage.class);
        when(message.getFeedbackId()).thenReturn(feedbackId);
        when(message.getConfig()).thenReturn(config);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(feedbackConfigChangeRTMProducer.buildFeedbackConfigChangeRTMConsumable(rtmClientContext, rootElementId, feedbackId, config))
                .thenReturn(feedbackConfigChangeRTMProducer);
        when(coursewareService.getRootElementId(feedbackId, FEEDBACK)).thenReturn(Mono.just(rootElementId));

        replaceInteractiveFeedbackConfigMessageHandler = new ReplaceInteractiveFeedbackConfigMessageHandler(feedbackService,
                                                                                                            coursewareService,
                                                                                                            rtmEventBrokerProvider,
                                                                                                            authenticationContextProvider,
                                                                                                            rtmClientContextProvider,
                                                                                                            feedbackConfigChangeRTMProducer);
    }

    @Test
    void validateMissingFeedbackId() {
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        replaceInteractiveFeedbackConfigMessageHandler.validate(new ReplaceInteractiveFeedbackConfigMessage()));
        assertEquals("feedbackId is required", response.getErrorMessage());
    }

    @Test
    void validateMissingConfig() {
        ReplaceInteractiveFeedbackConfigMessage m = mock(ReplaceInteractiveFeedbackConfigMessage.class);
        when(m.getFeedbackId()).thenReturn(feedbackId);
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        replaceInteractiveFeedbackConfigMessageHandler.validate(m));
        assertEquals("config is required", response.getErrorMessage());
    }

    @Test
    void validateSuccess() throws RTMValidationException {
        ReplaceInteractiveFeedbackConfigMessage m = mock(ReplaceInteractiveFeedbackConfigMessage.class);
        when(m.getFeedbackId()).thenReturn(feedbackId);
        when(m.getConfig()).thenReturn(config);
        replaceInteractiveFeedbackConfigMessageHandler.validate(m);
    }

    @Test
    void handle() throws WriteResponseException {
        FeedbackConfig feedbackConfig = new FeedbackConfig()
                .setId(feedbackConfigId)
                .setFeedbackId(feedbackId)
                .setConfig(config);

        when(feedbackService.replace(any(), any())).thenReturn(Mono.just(feedbackConfig));

        replaceInteractiveFeedbackConfigMessageHandler
                .handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture("{\"type\":" +
                "\"author.interactive.feedback.replace.ok\"," +
                "\"response\":{\"feedbackConfig\":{\"id\":\"" + feedbackConfigId + "\"," +
                "\"feedbackId\":\"" + feedbackId + "\"," +
                "\"config\":\"" + config + "\"}}}");
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_FEEDBACK_REPLACE), captor.capture());
        assertEquals(CoursewareAction.CONFIG_CHANGE, captor.getValue().getAction());
        assertEquals(feedbackId, captor.getValue().getElement().getElementId());
        assertEquals(FEEDBACK, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());

        verify(feedbackConfigChangeRTMProducer, atLeastOnce()).buildFeedbackConfigChangeRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(feedbackId), eq(config));
        verify(feedbackConfigChangeRTMProducer, atLeastOnce()).produce();
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_FeedbackNotFound() throws WriteResponseException {
        Mono mono = TestPublisher.create().error(new FeedbackNotFoundException(feedbackId)).mono();
        when(feedbackService.replace(any(), any())).thenReturn(mono);

        replaceInteractiveFeedbackConfigMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture("{\"type\":" +
                "\"author.interactive.feedback.replace.error\"," +
                "\"code\":404," +
                "\"message\":\"no feedback with id " + feedbackId + "\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(feedbackConfigChangeRTMProducer, never()).produce();
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_exception() throws WriteResponseException {
        Mono mono = TestPublisher.create().error(new RuntimeException("some other exception")).mono();
        when(feedbackService.replace(any(), any())).thenReturn(mono);

        replaceInteractiveFeedbackConfigMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture("{\"type\":" +
                "\"author.interactive.feedback.replace.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to replace feedback config\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(feedbackConfigChangeRTMProducer, never()).produce();
    }

}
