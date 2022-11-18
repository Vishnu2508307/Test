package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.message.handler.courseware.component.DeleteInteractiveComponentMessageHandler.AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ComponentParentNotFound;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.DeleteInteractiveComponentMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ComponentDeletedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class DeleteInteractiveComponentMessageHandlerTest {

    private DeleteInteractiveComponentMessageHandler handler;

    @Mock
    private DeleteInteractiveComponentMessage message;

    @Mock
    private ComponentService componentService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private CoursewareService coursewareService;

    private RTMEventBroker rtmEventBroker;

    @Mock
    private ComponentDeletedRTMProducer componentDeletedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private Session session;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final String messageId = "messageId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getComponentId()).thenReturn(componentId);
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getId()).thenReturn(messageId);

        when(coursewareService.getRootElementId(interactiveId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(componentDeletedRTMProducer.buildComponentDeletedRTMConsumable(rtmClientContext, rootElementId, componentId))
                .thenReturn(componentDeletedRTMProducer);

        handler = new DeleteInteractiveComponentMessageHandler(componentService,
                                                               rtmEventBrokerProvider,
                                                               authenticationContextProvider,
                                                               coursewareService,
                                                               rtmClientContextProvider,
                                                               componentDeletedRTMProducer);
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("interactiveId is required", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noComponentId() {
        when(message.getComponentId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("componentId is required", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_parentNotFound() {
        TestPublisher<ParentByComponent> publisher = TestPublisher.create();
        publisher.error(new ComponentParentNotFound(componentId));
        when(componentService.findParentFor(componentId)).thenReturn(publisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(String.format("no parent element for component with id %s", componentId), t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_parentNotSameType() {
        ParentByComponent parent = new ParentByComponent()
                .setParentType(CoursewareElementType.ACTIVITY);

        when(componentService.findParentFor(componentId)).thenReturn(Mono.just(parent));

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("parent component is not an INTERACTIVE", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_foundParentNotMatchingSuppliedParent() {
        ParentByComponent parent = new ParentByComponent()
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setParentId(UUID.randomUUID());

        when(componentService.findParentFor(componentId)).thenReturn(Mono.just(parent));

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(String.format("found interactive not matching interactiveId %s", interactiveId), t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void handle() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();

        when(componentService.deleteInteractiveComponent(componentId, interactiveId)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.component.delete.ok\"," +
                "\"response\":{" +
                "\"componentId\":\"" + componentId + "\"," +
                "\"interactiveId\":\"" + interactiveId + "\"" +
                "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(interactiveId, captor.getValue().getParentElement().getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, captor.getValue().getParentElement().getElementType());

        verify(componentDeletedRTMProducer, atLeastOnce()).buildComponentDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(componentId));
        verify(componentDeletedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));

        when(componentService.deleteInteractiveComponent(componentId, interactiveId)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.component.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"error deleting message\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(componentDeletedRTMProducer, never()).produce();
    }
}
