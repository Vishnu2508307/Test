package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.courseware.data.ScenarioLifecycle.ACTIVITY_EVALUATE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ScenarioByParent;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.eventmessage.ActivityEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
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
import com.smartsparrow.rtm.message.recv.courseware.scenario.ReorderScenariosMessage;
import com.smartsparrow.rtm.subscription.courseware.scenarioreordered.ScenarioReOrderedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ReorderScenariosMessageHandlerTest {

    @InjectMocks
    private ReorderScenariosMessageHandler handler;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private CamelReactiveStreamsService camel;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ScenarioReOrderedRTMProducer scenarioReOrderedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private ReorderScenariosMessage message;

    private static final String id = "message id";
    private static final UUID parentId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final ScenarioLifecycle lifecycle = ACTIVITY_EVALUATE;
    private static final UUID scenarioId = UUID.randomUUID();
    private static final List<UUID> scenarioIds = Lists.newArrayList(scenarioId);
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        message = mock(ReorderScenariosMessage.class);

        when(message.getId()).thenReturn(id);
        when(message.getParentId()).thenReturn(parentId);
        when(message.getLifecycle()).thenReturn(lifecycle);
        when(message.getScenarioIds()).thenReturn(scenarioIds);

        TestPublisher<Exchange> exchangePublisher = TestPublisher.create();
        when(camel.toStream(anyString(), any(ActivityEventMessage.class)))
                .thenReturn(exchangePublisher);

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(scenarioReOrderedRTMProducer.buildScenarioReOrderedRTMConsumable(rtmClientContext, rootElementId, parentId, INTERACTIVE, scenarioIds, lifecycle))
                .thenReturn(scenarioReOrderedRTMProducer);
        when(coursewareService.getRootElementId(parentId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        handler = new ReorderScenariosMessageHandler(scenarioService,
                                                     coursewareService,
                                                     rtmEventBrokerProvider,
                                                     authenticationContextProvider,
                                                     rtmClientContextProvider,
                                                     scenarioReOrderedRTMProducer);
    }


    @Test
    void validate_parentIdNotSupplied() {
        when(message.getParentId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertAll(() -> {
            assertEquals(id, e.getReplyTo());
            assertEquals("author.scenarios.reorder.error", e.getType());
            assertEquals("parentId is required", e.getErrorMessage());
        });

    }

    @Test
    void validate_scenarioIdsNotSupplied() {
        when(message.getScenarioIds()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertAll(() -> {
            assertEquals(id, e.getReplyTo());
            assertEquals("author.scenarios.reorder.error", e.getType());
            assertEquals("scenarioIds are required", e.getErrorMessage());
        });
    }

    @Test
    void validate_lifecycleNotSupplied() {
        when(message.getLifecycle()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertAll(() -> {
            assertEquals(id, e.getReplyTo());
            assertEquals("author.scenarios.reorder.error", e.getType());
            assertEquals("lifecycle is required", e.getErrorMessage());
        });
    }

    @Test
    void handle() throws WriteResponseException {
        when(scenarioService.reorder(parentId, lifecycle, scenarioIds, INTERACTIVE)).thenReturn(Mono.just(new ScenarioByParent()
                .setLifecycle(lifecycle)
                .setParentId(parentId)
                .setScenarioIds(scenarioIds)
                .setParentType(INTERACTIVE)));
        when(scenarioService.getScenarioParentTypeByLifecycle(lifecycle)).thenReturn("INTERACTIVE");

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.scenarios.reorder.ok\"," +
                "\"response\":{" +
                "\"scenariosByParent\":{" +
                "\"parentId\":\"" + parentId + "\"," +
                "\"lifecycle\":\"" + lifecycle + "\"," +
                "\"scenarioIds\":[" +
                "\"" + scenarioId + "\"" +
                "]" +
                "}" +
                "}," +
                "\"replyTo\":\"message id\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());

        assertEquals(CoursewareAction.SCENARIO_REORDERED, captor.getValue().getAction());
        assertEquals(lifecycle, captor.getValue().getScenarioLifecycle());
        assertEquals(parentId, captor.getValue().getElement().getElementId());
        assertEquals(INTERACTIVE, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());

        verify(scenarioReOrderedRTMProducer, atLeastOnce()).buildScenarioReOrderedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(parentId), eq(INTERACTIVE), eq(scenarioIds), eq(lifecycle));
        verify(scenarioReOrderedRTMProducer, atLeastOnce()).produce();
    }
}
