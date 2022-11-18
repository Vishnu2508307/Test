package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;
import static com.smartsparrow.courseware.data.ScenarioLifecycle.ACTIVITY_ENTRY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
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
import com.smartsparrow.courseware.data.ParentByScenario;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.scenario.ReplaceScenarioMessage;
import com.smartsparrow.rtm.subscription.courseware.updated.ScenarioUpdatedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class ReplaceScenarioMessageHandlerTest {

    private ReplaceScenarioMessageHandler replaceScenarioMessageHandler;

    private ReplaceScenarioMessage message;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private RTMEventBroker rtmEventBroker;

    @Mock
    private ScenarioUpdatedRTMProducer scenarioUpdatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID scenarioId = UUID.randomUUID();
    private static final String condition = "some condition";
    private static final String action = "some action";
    private static final UUID parentId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final ScenarioLifecycle lifecycle = ACTIVITY_ENTRY;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        message = mock(ReplaceScenarioMessage.class);
        when(message.getScenarioId()).thenReturn(scenarioId);
        when(message.getCondition()).thenReturn(condition);
        when(message.getActions()).thenReturn(action);

        Scenario scenario = buildScenario(message);
        ParentByScenario parent = new ParentByScenario().setScenarioId(scenarioId).setParentId(parentId).setParentType(
                ACTIVITY);

        when(scenarioService.updateScenario(message.getScenarioId(), message.getCondition(), message.getActions(),
                message.getName(), message.getDescription(), message.getCorrectness())).thenReturn(Mono.empty());
        when(scenarioService.findById(message.getScenarioId())).thenReturn(Mono.just(scenario));
        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(scenarioService.findParent(message.getScenarioId())).thenReturn(Mono.just(parent));
        when(coursewareService.getRootElementId(scenarioId, SCENARIO)).thenReturn(Mono.just(rootElementId));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(scenarioUpdatedRTMProducer.buildScenarioUpdatedRTMConsumable(rtmClientContext, rootElementId, scenarioId, parentId, ACTIVITY, lifecycle))
                .thenReturn(scenarioUpdatedRTMProducer);

        replaceScenarioMessageHandler = new ReplaceScenarioMessageHandler(
                scenarioService,
                coursewareService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                scenarioUpdatedRTMProducer
        );
    }

    @Test
    void validate_scenarioIdNotSupplied() {
        ReplaceScenarioMessage m = mock(ReplaceScenarioMessage.class);
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () -> replaceScenarioMessageHandler.validate(m));
        assertEquals("scenarioId is required", response.getErrorMessage());
    }

    @Test
    void validate_nameNotSupplied() {
        ReplaceScenarioMessage m = mock(ReplaceScenarioMessage.class);
        when(m.getScenarioId()).thenReturn(scenarioId);
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () -> replaceScenarioMessageHandler.validate(m));
        assertEquals("name is required", response.getErrorMessage());
    }

    @Test
    void validateSuccess() throws RTMValidationException {
        ReplaceScenarioMessage m = mock(ReplaceScenarioMessage.class);
        when(m.getScenarioId()).thenReturn(scenarioId);
        when(m.getName()).thenReturn("a name");
        when(m.getCondition()).thenReturn(condition);
        when(m.getActions()).thenReturn(action);
        replaceScenarioMessageHandler.validate(m);
    }

    @Test
    void handle() throws WriteResponseException {

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor
                .forClass(CoursewareElementBroadcastMessage.class);

        replaceScenarioMessageHandler.handle(session, message);

        verify(session.getRemote(), atLeastOnce())
                .sendStringByFuture(
                        "{" +
                                "\"type\":\"author.scenario.replace.ok\"," +
                                "\"response\":{" +
                                "\"scenario\":{" +
                                "\"id\":\"" + scenarioId + "\"," +
                                "\"condition\":\"some condition\"," +
                                "\"actions\":\"some action\"," +
                                "\"lifecycle\":\"ACTIVITY_ENTRY\"}}}"
                );

        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());

        assertNull(captor.getValue().getParentElement());
        assertEquals(scenarioId, captor.getValue().getElement().getElementId());
        assertEquals(CoursewareElementType.SCENARIO, captor.getValue().getElement().getElementType());

        verify(scenarioUpdatedRTMProducer, atLeastOnce()).buildScenarioUpdatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(scenarioId), eq(parentId), eq(ACTIVITY), eq(lifecycle));
        verify(scenarioUpdatedRTMProducer, atLeastOnce()).produce();
    }

    private Scenario buildScenario(ReplaceScenarioMessage message) {
        return new Scenario()
                .setId(message.getScenarioId())
                .setLifecycle(lifecycle)
                .setCondition(message.getCondition())
                .setActions(message.getActions())
                .setName(message.getName())
                .setDescription(message.getDescription());
    }
}
