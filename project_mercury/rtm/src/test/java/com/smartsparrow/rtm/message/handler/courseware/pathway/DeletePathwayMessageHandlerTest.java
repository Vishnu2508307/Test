package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.DeletePathwayMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.PathwayDeletedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class DeletePathwayMessageHandlerTest {

    private DeletePathwayMessageHandler handler;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private PathwayDeletedRTMProducer pathwayDeletedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private RTMEventBroker rtmEventBroker;
    private DeletePathwayMessage message;
    private static final String messageId = "80";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID parentActivityId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(DeletePathwayMessage.class);
        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(message.getId()).thenReturn(messageId);
        when(message.getParentActivityId()).thenReturn(parentActivityId);
        when(message.getPathwayId()).thenReturn(pathwayId);

        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(Mono.just(parentActivityId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(parentActivityId, ACTIVITY)).thenReturn(Mono.just(rootElementId));
        when(pathwayDeletedRTMProducer.buildPathwayDeletedRTMConsumable(rtmClientContext, rootElementId, pathwayId))
                .thenReturn(pathwayDeletedRTMProducer);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        handler = new DeletePathwayMessageHandler(pathwayService,
                                                  coursewareService,
                                                  rtmEventBrokerProvider,
                                                  authenticationContextProvider,
                                                  rtmClientContextProvider,
                                                  pathwayDeletedRTMProducer);
    }

    @Test
    void validate_pathwayIdNotSupplied() {
        when(message.getPathwayId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("pathwayId is required", e.getErrorMessage());
        assertEquals("author.pathway.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_parentActivityIdNotSupplied() {
        when(message.getParentActivityId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("parentActivityId is required", e.getErrorMessage());
        assertEquals("author.pathway.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_parentActivityNotFound() {

        TestPublisher<UUID> publisher = TestPublisher.create();
        publisher.error(new ParentActivityNotFoundException(pathwayId));
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(publisher.mono());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("parent activity not found for pathway " + pathwayId, e.getErrorMessage());
        assertEquals("author.pathway.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_parentActivityFoundNotMatching() {
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(Mono.just(UUID.randomUUID()));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("supplied parentActivityId does not match the pathway parent", e.getErrorMessage());
        assertEquals("author.pathway.delete.error", e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void delete_error() throws WriteResponseException {
        TestPublisher<Void> deleted = TestPublisher.create();
        deleted.error(new IllegalAccessException());

        when(pathwayService.delete(pathwayId, parentActivityId)).thenReturn(deleted.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.pathway.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"error deleting pathway\"," +
                "\"replyTo\":\"80\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(pathwayDeletedRTMProducer, never()).produce();
    }

    @Test
    void delete_success() throws WriteResponseException {
        TestPublisher<Void> deleted = TestPublisher.create();
        deleted.complete();
        when(pathwayService.delete(pathwayId, parentActivityId)).thenReturn(deleted.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.pathway.delete.ok\"," +
                "\"response\":{" +
                "\"parentActivityId\":\"" + parentActivityId + "\"," +
                "\"pathwayId\":\"" + pathwayId + "\"}," +
                "\"replyTo\":\"80\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(PATHWAY, captor.getValue().getElement().getElementType());
        assertEquals(pathwayId, captor.getValue().getElement().getElementId());
        assertEquals(parentActivityId, captor.getValue().getParentElement().getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getParentElement().getElementType());

        verify(pathwayDeletedRTMProducer, atLeastOnce()).buildPathwayDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(pathwayId));
        verify(pathwayDeletedRTMProducer, atLeastOnce()).produce();
    }

}
