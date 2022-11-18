package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.DeleteInteractiveMessageHandler.AUTHOR_INTERACTIVE_DELETE;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.DeleteInteractiveMessageHandler.AUTHOR_INTERACTIVE_DELETE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.DeleteInteractiveMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.InteractiveDeletedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DeleteInteractiveMessageHandlerTest {

    @InjectMocks
    private DeleteInteractiveMessageHandler handler;

    @Mock
    private DeleteInteractiveMessage message;

    @Mock
    private InteractiveService interactiveService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AnnotationService annotationService;

    @Mock
    private InteractiveDeletedRTMProducer interactiveDeletedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock(name = "rtmEventBrokerProvider")
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock(name = "rtmEventBroker")
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private Session session;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final String messageId = "message id";
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(interactiveService.findParentPathwayId(eq(interactiveId))).thenReturn(Mono.just(interactiveId));
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(message.getId()).thenReturn(messageId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(parentPathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        when(interactiveDeletedRTMProducer.buildInteractiveDeletedRTMConsumable(rtmClientContext,
                                                                                          rootElementId,
                                                                                          interactiveId,
                                                                                          parentPathwayId))
                .thenReturn(interactiveDeletedRTMProducer);

        handler = new DeleteInteractiveMessageHandler(interactiveService,
                                                      coursewareService,
                                                      rtmEventBrokerProvider,
                                                      authenticationContextProvider,
                                                      rtmClientContextProvider,
                                                      interactiveDeletedRTMProducer);
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("interactiveId is required", t.getErrorMessage());
        assertEquals(400, t.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_DELETE_ERROR, t.getType());
    }

    @Test
    void validate_noParentPathwayId() {
        when(message.getParentPathwayId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("parentPathwayId is required", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_wrongParentPathwayId() {

        TestPublisher<UUID> publisher = TestPublisher.create();
        publisher.emit(UUIDs.random());

        when(interactiveService.findParentPathwayId(eq(interactiveId))).thenReturn(publisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("supplied parentPathwayId does not match the interactive parent", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }


    @Test
    void handle_error() {
        TestPublisher<Void> deletedInteractive = TestPublisher.create();
        deletedInteractive.error(new IllegalAccessException("some msg"));

        when(interactiveService.delete(eq(interactiveId), eq(parentPathwayId))).thenReturn(deletedInteractive.mono());
        when(coursewareService.getRootElementId(parentPathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(rootElementId));
        when(annotationService.deleteAnnotation(rootElementId, interactiveId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"some msg\"," +
                "\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(interactiveDeletedRTMProducer, never()).produce();
    }

    @Test
    void handle_success() {
        TestPublisher<Void> deletedInteractive = TestPublisher.create();
        deletedInteractive.complete();
        when(annotationService.deleteAnnotation(rootElementId, interactiveId)).thenReturn(Flux.empty());
        when(coursewareService.getRootElementId(parentPathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(rootElementId));
        when(interactiveService.delete(eq(interactiveId), eq(parentPathwayId))).thenReturn(deletedInteractive.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.delete.ok\"," +
                "\"response\":{" +
                "\"parentPathwayId\":\"" + parentPathwayId + "\"," +
                "\"interactiveId\":\"" + interactiveId + "\"" +
                "},\"replyTo\":\"message id\"}";
        verify(interactiveService, atLeastOnce()).delete(interactiveId, parentPathwayId);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_DELETE), captor.capture());
        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(interactiveId, captor.getValue().getElement().getElementId());
        assertEquals(INTERACTIVE, captor.getValue().getElement().getElementType());
        assertEquals(parentPathwayId, captor.getValue().getParentElement().getElementId());
        assertEquals(CoursewareElementType.PATHWAY, captor.getValue().getParentElement().getElementType());

        verify(interactiveDeletedRTMProducer, atLeastOnce()).buildInteractiveDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(interactiveId), eq(parentPathwayId));
        verify(interactiveDeletedRTMProducer, atLeastOnce()).produce();
    }
}
