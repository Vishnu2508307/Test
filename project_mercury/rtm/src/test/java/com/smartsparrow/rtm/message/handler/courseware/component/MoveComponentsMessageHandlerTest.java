package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.message.handler.courseware.component.MoveComponentsMessageHandler.AUTHOR_COMPONENTS_MOVE_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
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

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
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
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.MoveComponentsMessage;
import com.smartsparrow.rtm.subscription.courseware.moved.ComponentMovedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class MoveComponentsMessageHandlerTest {

    private MoveComponentsMessageHandler handler;
    @Mock
    private MoveComponentsMessage message;
    @Mock
    private ComponentService componentService;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private ComponentMovedRTMProducer componentMovedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;

    private Session session;
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = INTERACTIVE;
    private static final String messageId = "messageId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = mock(Account.class);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getComponentIds()).thenReturn(Arrays.asList(componentId));
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getId()).thenReturn(messageId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));

        handler = new MoveComponentsMessageHandler(componentService,
                                                   coursewareService,
                                                   rtmEventBrokerProvider,
                                                   rtmClientContextProvider,
                                                   componentMovedRTMProducer, authenticationContextProvider);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noComponentId() {
        when(message.getComponentIds()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("componentIds are required", ex.getMessage());
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
    void handle() throws IOException {
        when(componentService.move(message.getComponentIds(), message.getElementId(), message.getElementType()))
                .thenReturn(Flux.empty());

        when(componentMovedRTMProducer.buildComponentMovedRTMConsumable(rtmClientContext,
                                                                        rootElementId,
                                                                        componentId))
                .thenReturn(componentMovedRTMProducer);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_COMPONENTS_MOVE_OK, response.getType());
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(
                CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(message.getType()), captor.capture());

        Assertions.assertEquals(CoursewareAction.COMPONENT_MOVED, captor.getValue().getAction());
        Assertions.assertEquals(accountId, captor.getValue().getAccountId());
        Assertions.assertEquals(CoursewareElementType.COMPONENT, captor.getValue().getElement().getElementType());
        Assertions.assertEquals(componentId, captor.getValue().getElement().getElementId());
        Assertions.assertEquals(elementId, captor.getValue().getParentElement().getElementId());
        Assertions.assertEquals(elementType, captor.getValue().getParentElement().getElementType());

        verify(componentMovedRTMProducer, atLeastOnce()).buildComponentMovedRTMConsumable(eq(rtmClientContext),
                                                                                          eq(rootElementId),
                                                                                          eq(componentId));
        verify(componentMovedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));

        when(componentService.move(Arrays.asList(componentId), elementId, elementType)).thenReturn(
                publisher.flux());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.components.move.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to move components\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(componentMovedRTMProducer, never()).produce();
    }
}
