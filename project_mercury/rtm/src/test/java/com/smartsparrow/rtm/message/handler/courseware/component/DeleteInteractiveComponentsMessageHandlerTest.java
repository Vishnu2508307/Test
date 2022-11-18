package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.message.handler.courseware.component.DeleteInteractiveComponentsMessageHandler.AUTHOR_INTERACTIVE_COMPONENTS_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.DeleteInteractiveComponentsMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ComponentDeletedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class DeleteInteractiveComponentsMessageHandlerTest {

    private DeleteInteractiveComponentsMessageHandler handler;

    @Mock
    private DeleteInteractiveComponentsMessage message;

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

        when(message.getComponentIds()).thenReturn(Arrays.asList(componentId));
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
        when(componentDeletedRTMProducer.buildComponentDeletedRTMConsumable(rtmClientContext,
                                                                            rootElementId,
                                                                            componentId))
                .thenReturn(componentDeletedRTMProducer);

        handler = new DeleteInteractiveComponentsMessageHandler(componentService,
                                                                rtmEventBrokerProvider,
                                                                authenticationContextProvider,
                                                                coursewareService,
                                                                rtmClientContextProvider,
                                                                componentDeletedRTMProducer);
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("interactiveId is required", ex.getMessage());
    }

    @Test
    void validate_noComponentId() {
        when(message.getComponentIds()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("componentId is required", ex.getMessage());
    }

    @Test
    void validate_parentNotFound() {
        TestPublisher<List<ParentByComponent>> publisher = TestPublisher.create();
        publisher.error(new ComponentParentNotFound(componentId));
        when(componentService.findParentForComponents(Arrays.asList(componentId))).thenReturn(publisher.mono());

        ComponentParentNotFound ex = assertThrows(ComponentParentNotFound.class, () -> handler.validate(message));
        assertEquals("no parent element for component with id " + componentId, ex.getMessage());
    }

    @Test
    void validate_parentNotSameType() {
        ParentByComponent parent = new ParentByComponent()
                .setParentType(CoursewareElementType.ACTIVITY);

        when(componentService.findParentForComponents(Arrays.asList(componentId))).thenReturn(Mono.just(Arrays.asList(
                parent)));

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("parent component is not an INTERACTIVE", ex.getMessage());
    }

    @Test
    void validate_foundParentNotMatchingSuppliedParent() {
        ParentByComponent parent = new ParentByComponent()
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setParentId(UUID.randomUUID());

        when(componentService.findParentForComponents(Arrays.asList(componentId))).thenReturn(Mono.just(Arrays.asList(
                parent)));

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("found interactive not matching interactiveId " + interactiveId, ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(componentService.deleteInteractiveComponents(message.getComponentIds(), message.getInteractiveId()))
                .thenReturn(Flux.empty());

        when(componentDeletedRTMProducer.buildComponentDeletedRTMConsumable(rtmClientContext,
                                                                            rootElementId,
                                                                            componentId))
                .thenReturn(componentDeletedRTMProducer);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_INTERACTIVE_COMPONENTS_DELETE_OK, response.getType());
                List componentIds = (List) response.getResponse().get("componentIds");
                assertNotNull(componentIds);

            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(
                CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(message.getType()), captor.capture());

        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(interactiveId, captor.getValue().getParentElement().getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, captor.getValue().getParentElement().getElementType());

        verify(componentDeletedRTMProducer, atLeastOnce()).buildComponentDeletedRTMConsumable(eq(rtmClientContext),
                                                                                              eq(rootElementId),
                                                                                              eq(componentId));
        verify(componentDeletedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));

        when(componentService.deleteInteractiveComponents(Arrays.asList(componentId), interactiveId)).thenReturn(
                publisher.flux());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.components.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to delete components for an interactive\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(componentDeletedRTMProducer, never()).produce();
    }
}
