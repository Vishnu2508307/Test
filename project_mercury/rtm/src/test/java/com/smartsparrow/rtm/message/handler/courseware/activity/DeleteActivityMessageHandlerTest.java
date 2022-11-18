package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.DeleteActivityMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ActivityDeletedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DeleteActivityMessageHandlerTest {

    @InjectMocks
    private DeleteActivityMessageHandler handler;

    @Mock
    private ActivityService activityService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private ActivityDeletedRTMProducer activityDeletedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private DeleteActivityMessage message;
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final String messageId = "message id";
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private RTMEventBroker rtmEventBroker;
    private final UUID accountId = UUID.randomUUID();
    private final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(DeleteActivityMessage.class);
        rtmEventBroker = mock(RTMEventBroker.class);
        Activity activity = mock(Activity.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.just(parentPathwayId));
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(activityDeletedRTMProducer.buildActivityDeletedRTMConsumable(rtmClientContext, rootElementId, activityId, parentPathwayId))
                .thenReturn(activityDeletedRTMProducer);
        when(coursewareService.getRootElementId(parentPathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        handler = new DeleteActivityMessageHandler(activityService,
                                                   coursewareService,
                                                   rtmEventBrokerProvider,
                                                   authenticationContextProvider,
                                                   rtmClientContextProvider,
                                                   activityDeletedRTMProducer);
    }

    @Test
    void validate_activityIdNotSupplied() {
        when(message.getActivityId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("activityId is required", e.getErrorMessage());
        assertEquals("author.activity.delete.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_parentPathwayIdNotSupplied() {
        when(message.getParentPathwayId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("parentPathwayId is required", e.getErrorMessage());
        assertEquals("author.activity.delete.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_activityNotFound() {
        when(activityService.findById(activityId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(String.format("activity %s not found", activityId), e.getErrorMessage());
        assertEquals("author.activity.delete.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_activityHasNoParent() {
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(String.format("parentPathwayId not found for activity %s", activityId)
                , e.getErrorMessage());
        assertEquals("author.activity.delete.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_parentPathwayIdNotMatching() {
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.just(UUID.randomUUID()));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("supplied parentPathwayId does not match the activity parent", e.getErrorMessage());
        assertEquals("author.activity.delete.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> deletedActivity = TestPublisher.create();
        deletedActivity.error(new IllegalAccessException());

        when(activityService.delete(activityId, parentPathwayId, accountId)).thenReturn(deletedActivity.flux());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"error deleting activity\"," +
                "\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_success() throws WriteResponseException {
        TestPublisher<Void> deletedActivity = TestPublisher.create();
        deletedActivity.complete();
        when(activityService.delete(activityId, parentPathwayId, accountId)).thenReturn(deletedActivity.flux());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.delete.ok\"," +
                "\"response\":{" +
                "\"activityId\":\"" + activityId + "\"," +
                "\"parentPathwayId\":\"" + parentPathwayId + "\"" +
                "},\"replyTo\":\"message id\"}";
        verify(activityService, atLeastOnce()).delete(activityId, parentPathwayId, accountId);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getElement().getElementType());
        assertEquals(activityId, captor.getValue().getElement().getElementId());
        assertEquals(parentPathwayId, captor.getValue().getParentElement().getElementId());
        assertEquals(PATHWAY, captor.getValue().getParentElement().getElementType());

        verify(activityDeletedRTMProducer, atLeastOnce()).buildActivityDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(activityId), eq(parentPathwayId));
        verify(activityDeletedRTMProducer, atLeastOnce()).produce();
    }
}
