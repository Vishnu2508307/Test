package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.rtm.message.handler.courseware.component.SetManualGradeConfigurationMessageHandler.AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET;
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
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
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
import com.smartsparrow.rtm.message.recv.courseware.component.ManualGradingConfigurationSetMessage;
import com.smartsparrow.rtm.subscription.courseware.manualgrading.ComponentConfigurationCreatedRTMProducer;
import com.smartsparrow.rtm.subscription.courseware.message.ManualGradingConfig;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class SetManualGradeConfigurationMessageHandlerTest {

    @InjectMocks
    private SetManualGradeConfigurationMessageHandler handler;

    @Mock
    private ComponentService componentService;

    @Mock
    private ManualGradeService manualGradeService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ManualGradingConfigurationSetMessage message;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ComponentConfigurationCreatedRTMProducer componentConfigurationCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final Double maxScore = 10d;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getType()).thenReturn(AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET);
        when(message.getComponentId()).thenReturn(componentId);
        when(message.getMaxScore()).thenReturn(maxScore);

        when(componentService.findById(componentId)).thenReturn(Mono.just(new Component()));
        when(coursewareService.getRootElementId(componentId, COMPONENT)).thenReturn(Mono.just(rootElementId));

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        ManualGradingConfig manualGradingConfig = new ManualGradingConfig().setComponentId(
                componentId).setMaxScore(maxScore);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(componentConfigurationCreatedRTMProducer.buildComponentConfigurationCreatedRTMConsumable(rtmClientContext,
                                                                                                      rootElementId,
                                                                                                      componentId,
                                                                                                      manualGradingConfig))
                .thenReturn(componentConfigurationCreatedRTMProducer);

        handler = new SetManualGradeConfigurationMessageHandler(componentService,
                                                                manualGradeService,
                                                                rtmEventBrokerProvider,
                                                                authenticationContextProvider,
                                                                coursewareService,
                                                                rtmClientContextProvider,
                                                                componentConfigurationCreatedRTMProducer);
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

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(e);
        assertEquals("no component with id " + componentId, e.getMessage());
    }

    @Test
    void handle() throws WriteResponseException {
        ManualGradingConfiguration manualGradingConfiguration = new ManualGradingConfiguration().setComponentId(componentId).setMaxScore(maxScore);
        when(manualGradeService.createManualGradingConfiguration(componentId, maxScore))
                .thenReturn(Mono.just(manualGradingConfiguration));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.component.manual.grading.configuration.set.ok\"," +
                "\"response\":{" +
                "\"manualGradingConfiguration\":{" +
                "\"componentId\":\"" + componentId + "\"," +
                "\"maxScore\":10.0" +
                "}" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET), captor.capture());
        assertEquals(CoursewareAction.MANUAL_GRADING_CONFIGURATION_CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertNull(captor.getValue().getParentElement());

        verify(componentConfigurationCreatedRTMProducer, atLeastOnce()).buildComponentConfigurationCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(componentId), eq(ManualGradingConfig.from(manualGradingConfiguration)));
        verify(componentConfigurationCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<ManualGradingConfiguration> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("foo"));

        when(manualGradeService.createManualGradingConfiguration(componentId, maxScore)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.component.manual.grading.configuration.set.error\"," +
                "\"code\":422," +
                "\"message\":\"foo\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmEventBroker, never()).broadcast(any(String.class), any(CoursewareElementBroadcastMessage.class));
        verify(componentConfigurationCreatedRTMProducer, never()).produce();
    }
}
