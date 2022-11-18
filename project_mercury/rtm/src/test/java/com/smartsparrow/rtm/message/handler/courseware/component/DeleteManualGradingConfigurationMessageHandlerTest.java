package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.rtm.message.handler.courseware.component.DeleteManualGradingConfigurationMessageHandler.AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.ComponentMessage;
import com.smartsparrow.rtm.subscription.courseware.manualgrading.ComponentManualGradingConfigDeletedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DeleteManualGradingConfigurationMessageHandlerTest {

    @InjectMocks
    private DeleteManualGradingConfigurationMessageHandler handler;

    @Mock
    private ComponentService componentService;

    @Mock
    private ManualGradeService manualGradeService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ComponentMessage message;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ComponentManualGradingConfigDeletedRTMProducer componentManualGradingConfigDeletedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getType()).thenReturn(AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE);
        when(message.getComponentId()).thenReturn(componentId);

        when(componentService.findById(componentId)).thenReturn(Mono.just(new Component()));
        when(coursewareService.getRootElementId(componentId, COMPONENT)).thenReturn(Mono.just(rootElementId));

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(componentManualGradingConfigDeletedRTMProducer.buildManualGradingConfigDeletedRTMConsumable(
                rtmClientContext,
                rootElementId,
                componentId))
                .thenReturn(componentManualGradingConfigDeletedRTMProducer);

        handler = new DeleteManualGradingConfigurationMessageHandler(componentService,
                                                                     manualGradeService,
                                                                     rtmEventBrokerProvider,
                                                                     authenticationContextProvider,
                                                                     coursewareService,
                                                                     rtmClientContextProvider,
                                                                     componentManualGradingConfigDeletedRTMProducer);
    }

    @Test
    void validate_nullComponentId() {
        when(message.getComponentId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(e);
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void validate_componentNotFound() {
        TestPublisher<Component> publisher = TestPublisher.create();
        publisher.error(new ComponentNotFoundException(componentId));

        when(componentService.findById(componentId)).thenReturn(publisher.mono());
        assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
    }

    @Test
    void handler_error() throws WriteResponseException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("dmt"));
        when(manualGradeService.deleteManualGradingConfiguration(componentId)).thenReturn(publisher.flux());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.component.manual.grading.configuration.delete.error\"," +
                "\"code\":422," +
                "\"message\":\"dmt\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmEventBroker, never()).broadcast(any(String.class), any(CoursewareElementBroadcastMessage.class));
        verify(componentManualGradingConfigDeletedRTMProducer, never()).produce();
    }

    @Test
    void handle() throws WriteResponseException {
        when(manualGradeService.deleteManualGradingConfiguration(componentId)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.component.manual.grading.configuration.delete.ok\"," +
                "\"response\":{" +
                "\"manualGradingConfiguration\":{" +
                "\"componentId\":\"" + componentId + "\"" +
                "}" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE), captor.capture());
        assertEquals(CoursewareAction.MANUAL_GRADING_CONFIGURATION_DELETED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertNull(captor.getValue().getParentElement());

        verify(componentManualGradingConfigDeletedRTMProducer, atLeastOnce()).buildManualGradingConfigDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(componentId));
        verify(componentManualGradingConfigDeletedRTMProducer, atLeastOnce()).produce();
    }

}
