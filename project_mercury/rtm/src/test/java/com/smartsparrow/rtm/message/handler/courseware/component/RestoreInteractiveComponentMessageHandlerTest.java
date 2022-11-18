package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.message.handler.courseware.component.RestoreInteractiveComponentMessageHandler.AUTHOR_INTERACTIVE_COMPONENT_RESTORE_OK;
import static com.smartsparrow.rtm.message.handler.courseware.component.RestoreInteractiveComponentMessageHandler.AUTHOR_INTERACTIVE_COMPONENT_RESTORE_ERROR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.RestoreInteractiveComponentMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ComponentCreatedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

public class RestoreInteractiveComponentMessageHandlerTest {
    private Session session;

    RestoreInteractiveComponentMessageHandler handler;

    @Mock
    ComponentService componentService;

    @Mock
    InteractiveService interactiveService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ComponentCreatedRTMProducer componentCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private RestoreInteractiveComponentMessage message;

    private RTMEventBroker rtmEventBroker;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getComponentIds()).thenReturn(Arrays.asList(componentId));

        session = RTMWebSocketTestUtils.mockSession();

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.just(new Interactive().setId(interactiveId)));
        when(componentService.findById(eq(componentId))).thenReturn(Mono.just(new Component().setId(componentId)));

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        when(coursewareService.getRootElementId(interactiveId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                                                                    .setId(accountId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        handler = new RestoreInteractiveComponentMessageHandler(
                componentService,
                interactiveService,
                rtmEventBrokerProvider,
                coursewareService,
                authenticationContextProvider,
                rtmClientContextProvider,
                componentCreatedRTMProducer);
    }

    @Test
    void validateMissingInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("interactiveId is required", ex.getMessage());
    }

    @Test
    void validateMissingComponentId() {
        when(message.getComponentIds()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("componentId is required", ex.getMessage());
    }

    @Test
    void validateInteractiveNotFound() {
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(Mono.empty());
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("interactive not found", ex.getMessage());
    }

    @Test
    void handle() throws IOException {

        when(componentService.restoreComponent(message.getComponentIds(), message.getInteractiveId(), INTERACTIVE))
                .thenReturn(Flux.just(new Component().setId(componentId)));

        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext,
                                                                            rootElementId,
                                                                            componentId))
                .thenReturn(componentCreatedRTMProducer);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_INTERACTIVE_COMPONENT_RESTORE_OK, response.getType());
                List componentMap = (List) response.getResponse().get("components");
                assertNotNull(componentMap);
                assertEquals(1, componentMap.size());
                assertEquals(componentId.toString(), ((Map) componentMap.get(0)).get("id"));
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(
                CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(message.getType()), captor.capture());

        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(interactiveId, captor.getValue().getParentElement().getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, captor.getValue().getParentElement().getElementType());

        verify(componentCreatedRTMProducer, atLeastOnce()).buildComponentCreatedRTMConsumable(eq(rtmClientContext),
                                                                                              eq(rootElementId),
                                                                                              eq(componentId));
        verify(componentCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() {
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(componentService.restoreComponent(message.getComponentIds(), message.getInteractiveId(), INTERACTIVE))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_INTERACTIVE_COMPONENT_RESTORE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to restore component for an interactive\"}");
    }
}
