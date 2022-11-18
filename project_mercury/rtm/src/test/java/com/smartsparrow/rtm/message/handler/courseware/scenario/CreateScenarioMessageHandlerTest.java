package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.ScenarioCorrectness.correct;
import static com.smartsparrow.courseware.data.ScenarioLifecycle.ACTIVITY_ENTRY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.eventmessage.ActivityEventMessage;
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
import com.smartsparrow.rtm.message.recv.courseware.scenario.CreateScenarioMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ScenarioCreatedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

public class CreateScenarioMessageHandlerTest {

    private CreateScenarioMessageHandler createScenarioMessageHandler;

    private CreateScenarioMessage message;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private CamelReactiveStreamsService camel;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ScenarioCreatedRTMProducer scenarioCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private RTMEventBroker rtmEventBroker;
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID scenarioId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final ScenarioLifecycle lifecycle = ACTIVITY_ENTRY;
    private static final String condition = "some condition";
    private static final String action = "some action";
    private static final String name = "name";
    private static final String description = "description";
    private static final ScenarioCorrectness correctness = correct;
    private static final UUID parentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        message = mock(CreateScenarioMessage.class);
        when(message.getCondition()).thenReturn(condition);
        when(message.getActions()).thenReturn(action);
        when(message.getLifecycle()).thenReturn(lifecycle);
        when(message.getName()).thenReturn(name);
        when(message.getDescription()).thenReturn(description);
        when(message.getCorrectness()).thenReturn(correctness);
        when(message.getParentId()).thenReturn(parentId);
        when(message.getElementId()).thenReturn(scenarioId);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        TestPublisher<Exchange> exchangePublisher = TestPublisher.create();
        when(camel.toStream(anyString(), any(ActivityEventMessage.class)))
                .thenReturn(exchangePublisher);

        when(scenarioService.getScenarioParentTypeByLifecycle(lifecycle)).thenReturn("ACTIVITY");

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(coursewareService.getRootElementId(parentId, ACTIVITY)).thenReturn(Mono.just(rootElementId));

        when(scenarioCreatedRTMProducer.buildScenarioCreatedRTMConsumable(rtmClientContext, rootElementId, scenarioId, parentId, ACTIVITY, lifecycle))
                .thenReturn(scenarioCreatedRTMProducer);

        createScenarioMessageHandler = new CreateScenarioMessageHandler(
                scenarioService,
                coursewareService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                scenarioCreatedRTMProducer
        );
    }

    @Test
    void validate_lifecycleNotSupplied() {
        when(message.getLifecycle()).thenReturn(null);

        RTMValidationException response =
                assertThrows(RTMValidationException.class, () -> createScenarioMessageHandler.validate(message));
        assertEquals("lifecycle is required", response.getErrorMessage());
    }

    @Test
    void validate_nameNotSupplied() {
        when(message.getName()).thenReturn(null);

        RTMValidationException response =
                assertThrows(RTMValidationException.class, () -> createScenarioMessageHandler.validate(message));
        assertEquals("name is required", response.getErrorMessage());
    }

    @Test
    void validate_parentIdNotSupplied() {
        when(message.getParentId()).thenReturn(null);

        RTMValidationException response =
                assertThrows(RTMValidationException.class, () -> createScenarioMessageHandler.validate(message));
        assertEquals("parentId is required", response.getErrorMessage());
    }

    @Test
    void validateSuccess() throws RTMValidationException {
        createScenarioMessageHandler.validate(message);
    }

    @Test
    void handle() throws WriteResponseException {
        Scenario scenario = new Scenario()
                .setActions(action)
                .setCondition(condition)
                .setId(scenarioId)
                .setLifecycle(lifecycle);
        when(scenarioService.create(any(String.class), any(String.class), any(String.class), any(String.class),
                any(ScenarioCorrectness.class), eq(lifecycle), eq(parentId), eq(ACTIVITY)))
                .thenReturn(Mono.just(scenario));

        createScenarioMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.scenario.create.ok\"," +
                "\"response\":{" +
                "\"scenario\":{" +
                "\"id\":\"" + scenarioId + "\"," +
                "\"condition\":\"some condition\"," +
                "\"actions\":\"some action\"," +
                "\"lifecycle\":\"ACTIVITY_ENTRY\"}}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());

        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getParentElement().getElementType());
        assertEquals(CoursewareElementType.SCENARIO, captor.getValue().getElement().getElementType());

        verify(scenarioCreatedRTMProducer, atLeastOnce()).buildScenarioCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(scenario.getId()), eq(parentId), eq(ACTIVITY), eq(scenario.getLifecycle()));
        verify(scenarioCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handleWithNullAction() throws WriteResponseException {
        Scenario scenario = new Scenario()
                .setActions(null)
                .setCondition(condition)
                .setId(scenarioId)
                .setLifecycle(lifecycle);

        when(message.getActions()).thenReturn(null);
        when(scenarioService.create(any(String.class), eq(null), any(String.class), any(String.class),
                any(ScenarioCorrectness.class), eq(lifecycle), eq(parentId), eq(ACTIVITY)))
                .thenReturn(Mono.just(scenario));

        createScenarioMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.scenario.create.ok\"," +
                "\"response\":{" +
                "\"scenario\":{" +
                "\"id\":\"" + scenarioId + "\"," +
                "\"condition\":\"some condition\"," +
                "\"lifecycle\":\"ACTIVITY_ENTRY\"}}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(scenarioCreatedRTMProducer, atLeastOnce()).buildScenarioCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(scenarioId), eq(parentId), eq(ACTIVITY), eq(lifecycle));
        verify(scenarioCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handleWithNullCondition() throws WriteResponseException {
        Scenario scenario = new Scenario()
                .setActions(action)
                .setCondition(null)
                .setId(scenarioId)
                .setLifecycle(lifecycle);
        when(message.getCondition()).thenReturn(null);
        when(scenarioService.create(eq(null), any(String.class), any(String.class), any(String.class),
                any(ScenarioCorrectness.class), eq(lifecycle), eq(parentId), eq(ACTIVITY)))
                .thenReturn(Mono.just(scenario));

        createScenarioMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.scenario.create.ok\"," +
                "\"response\":{" +
                "\"scenario\":{" +
                "\"id\":\"" + scenarioId + "\"," +
                "\"actions\":\"some action\"," +
                "\"lifecycle\":\"ACTIVITY_ENTRY\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(scenarioCreatedRTMProducer, atLeastOnce()).buildScenarioCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(scenarioId), eq(parentId), eq(ACTIVITY), eq(lifecycle));
        verify(scenarioCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handleWithNullConditionAndAction() throws WriteResponseException {
        Scenario scenario = new Scenario()
                .setActions(null)
                .setCondition(null)
                .setId(scenarioId)
                .setLifecycle(lifecycle);

        when(message.getActions()).thenReturn(null);
        when(message.getCondition()).thenReturn(null);
        when(scenarioService.create(eq(null), eq(null), any(String.class), any(String.class),
                any(ScenarioCorrectness.class), eq(lifecycle), eq(parentId), eq(ACTIVITY)))
                .thenReturn(Mono.just(scenario));

        createScenarioMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.scenario.create.ok\"," +
                "\"response\":{" +
                "\"scenario\":{" +
                "\"id\":\"" + scenarioId + "\"," +
                "\"lifecycle\":\"ACTIVITY_ENTRY\"" +
                "}" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(scenarioCreatedRTMProducer, atLeastOnce()).buildScenarioCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(scenarioId), eq(parentId), eq(ACTIVITY), eq(lifecycle));
        verify(scenarioCreatedRTMProducer, atLeastOnce()).produce();
    }
}
