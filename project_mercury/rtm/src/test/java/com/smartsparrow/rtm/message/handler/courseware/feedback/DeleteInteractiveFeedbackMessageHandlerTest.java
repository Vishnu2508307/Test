package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.DeleteInteractiveFeedbackMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_DELETE;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.DeleteInteractiveFeedbackMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.DeleteInteractiveFeedbackMessageHandler.AUTHOR_INTERACTIVE_FEEDBACK_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.ParentInteractiveNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.feedback.DeleteInteractiveFeedbackMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.FeedbackDeletedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DeleteInteractiveFeedbackMessageHandlerTest {

    @InjectMocks
    private DeleteInteractiveFeedbackMessageHandler handler;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private FeedbackDeletedRTMProducer feedbackDeletedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private DeleteInteractiveFeedbackMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private Session session;

    private static final UUID feedbackId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getFeedbackId()).thenReturn(feedbackId);
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(coursewareService.getRootElementId(interactiveId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                                                                    .setId(UUID.randomUUID()));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(feedbackDeletedRTMProducer.buildFeedbackDeletedRTMConsumable(rtmClientContext, rootElementId, feedbackId))
                .thenReturn(feedbackDeletedRTMProducer);

        handler = new DeleteInteractiveFeedbackMessageHandler(feedbackService,
                                                              coursewareService,
                                                              rtmEventBrokerProvider,
                                                              authenticationContextProvider,
                                                              rtmClientContextProvider,
                                                              feedbackDeletedRTMProducer);
    }

    @Test
    void validate_noFeedbackId() {
        when(message.getFeedbackId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("feedbackId is required", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("interactiveId is required", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_feedbackNotFound() {
        TestPublisher<Feedback> feedbackPublisher = TestPublisher.create();
        feedbackPublisher.error(new FeedbackNotFoundException(feedbackId));
        when(feedbackService.findById(feedbackId)).thenReturn(feedbackPublisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("no feedback with id " + feedbackId, t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_feedbackNotHaveParent() {
        when(feedbackService.findById(feedbackId)).thenReturn(Mono.just(new Feedback().setId(feedbackId)));
        TestPublisher<UUID> parentPublisher = TestPublisher.create();
        parentPublisher.error(new ParentInteractiveNotFoundException(feedbackId));
        when(feedbackService.findParentId(feedbackId)).thenReturn(parentPublisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("parent interactive not found for feedback " + feedbackId, t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_incorrectParent() {
        when(feedbackService.findById(feedbackId)).thenReturn(Mono.just(new Feedback().setId(feedbackId)));
        when(feedbackService.findParentId(feedbackId)).thenReturn(Mono.just(UUID.randomUUID()));

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("supplied interactiveId does not match the feedback parent", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate() throws RTMValidationException {
        when(feedbackService.findById(feedbackId)).thenReturn(Mono.just(new Feedback().setId(feedbackId)));
        when(feedbackService.findParentId(feedbackId)).thenReturn(Mono.just(interactiveId));

        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        when(feedbackService.delete(feedbackId, interactiveId)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertEquals(AUTHOR_INTERACTIVE_FEEDBACK_DELETE_OK, response.getType());
            assertEquals(feedbackId.toString(), response.getResponse().get("feedbackId"));
            assertEquals(interactiveId.toString(), response.getResponse().get("interactiveId"));
        });
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_FEEDBACK_DELETE), captor.capture());
        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(feedbackId, captor.getValue().getElement().getElementId());
        assertEquals(CoursewareElementType.FEEDBACK, captor.getValue().getElement().getElementType());
        assertEquals(interactiveId, captor.getValue().getParentElement().getElementId());
        assertEquals(INTERACTIVE, captor.getValue().getParentElement().getElementType());

        verify(feedbackDeletedRTMProducer, atLeastOnce()).buildFeedbackDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(feedbackId));
        verify(feedbackDeletedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(feedbackService.delete(feedbackId, interactiveId)).thenReturn(publisher.flux());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR + "\",\"code\":422," +
                "\"message\":\"some exception\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(feedbackDeletedRTMProducer, never()).produce();
    }
}
