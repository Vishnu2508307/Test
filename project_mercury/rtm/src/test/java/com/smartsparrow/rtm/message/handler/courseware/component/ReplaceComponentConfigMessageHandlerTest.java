package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
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

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.ReplaceComponentConfigMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.ComponentConfigChangeRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

public class ReplaceComponentConfigMessageHandlerTest {

    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private ReplaceComponentConfigMessageHandler replaceComponentConfigMessageHandler;

    @Mock
    ComponentService componentService;

    @Mock
    CoursewareService coursewareService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private RTMEventBroker rtmEventBroker;

    @Mock
    private ComponentConfigChangeRTMProducer componentConfigChangeRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID componentConfigId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static String config = "foo";

    private ReplaceComponentConfigMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        message = mock(ReplaceComponentConfigMessage.class);
        when(message.getComponentId()).thenReturn(componentId);
        when(message.getConfig()).thenReturn(config);

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();
        when(coursewareService.saveConfigurationFields(any(UUID.class), any(String.class))).thenReturn(Flux.just(new Void[]{}));
        when(coursewareService.getRootElementId(componentId, COMPONENT)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(componentConfigChangeRTMProducer.buildComponentConfigChangeRTMConsumable(rtmClientContext,
                                                                                      rootElementId,
                                                                                      componentId,
                                                                                      config))
                .thenReturn(componentConfigChangeRTMProducer);

        replaceComponentConfigMessageHandler = new ReplaceComponentConfigMessageHandler(componentService,
                                                                                        rtmEventBrokerProvider,
                                                                                        coursewareService,
                                                                                        authenticationContextProvider,
                                                                                        rtmClientContextProvider,
                                                                                        componentConfigChangeRTMProducer);
    }

    @Test
    void validateMissingComponentId() {
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        replaceComponentConfigMessageHandler.validate(new ReplaceComponentConfigMessage()));
        assertEquals("componentId is missing", response.getErrorMessage());
    }

    @Test
    void validateMissingComponentConfig() {
        ReplaceComponentConfigMessage m = mock(ReplaceComponentConfigMessage.class);
        when(m.getComponentId()).thenReturn(componentId);
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        replaceComponentConfigMessageHandler.validate(m));
        assertEquals("config is missing", response.getErrorMessage());
    }

    @Test
    void validate_Success() throws RTMValidationException {
        ReplaceComponentConfigMessage m = mock(ReplaceComponentConfigMessage.class);
        when(m.getComponentId()).thenReturn(componentId);
        when(m.getConfig()).thenReturn(config);
        replaceComponentConfigMessageHandler.validate(m);
    }

    @Test
    void handle() throws WriteResponseException {
        ComponentConfig componentConfig = new ComponentConfig()
                .setId(componentConfigId)
                .setComponentId(componentId)
                .setConfig(config);

        when(componentService.replaceConfig(any(UUID.class), any(String.class)))
                .thenReturn(Mono.just(componentConfig));

        replaceComponentConfigMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(
                "{\"type\":\"author.component.replace.ok\"," +
                        "\"response\":{\"componentConfig\":" +
                        "{\"id\":\"" + componentConfigId + "\"," +
                        "\"componentId\":\"" + componentId + "\"," +
                        "\"config\":\"" + config + "\"}}}"
        );

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        verify(coursewareService).saveConfigurationFields(any(UUID.class), eq(config));

        assertEquals(CoursewareAction.CONFIG_CHANGE, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertNull(captor.getValue().getParentElement());

        verify(componentConfigChangeRTMProducer, atLeastOnce()).buildComponentConfigChangeRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(componentId), eq(config));
        verify(componentConfigChangeRTMProducer, atLeastOnce()).produce();
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_ComponentNotFound() throws WriteResponseException {
        Mono mono = TestPublisher.create().error(new ComponentNotFoundException(componentId)).mono();

        when(componentService.replaceConfig(any(UUID.class), any(String.class)))
                .thenReturn(mono);

        replaceComponentConfigMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(
                "{\"type\":\"author.component.replace.error\"," +
                        "\"code\":400," +
                        "\"message\":\"no component with id " + componentId + "\"}"
        );
        verify(componentConfigChangeRTMProducer, never()).produce();

    }
}
