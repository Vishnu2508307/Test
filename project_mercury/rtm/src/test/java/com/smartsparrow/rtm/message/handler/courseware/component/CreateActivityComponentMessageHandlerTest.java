package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.rtm.message.handler.courseware.component.CreateActivityComponentMessageHandler.AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.CreateActivityComponentMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ComponentCreatedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class CreateActivityComponentMessageHandlerTest {

    private CreateActivityComponentMessageHandler handler;

    @Mock
    private CreateActivityComponentMessage message;

    @Mock
    private ActivityService activityService;

    @Mock
    private ComponentService componentService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private PluginService pluginService;

    @Mock
    private ComponentCreatedRTMProducer componentCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private RTMEventBroker rtmEventBroker;
    private Session session;
    private static final String messageId = "messageId";
    private static final String pluginVersionExpr = "1.*";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final String config = "some config";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getId()).thenReturn(messageId);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn(pluginVersionExpr);

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(coursewareService.saveConfigurationFields(any(UUID.class), any(String.class))).thenReturn(Flux.just(new Void[]{}));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, componentId))
                .thenReturn(componentCreatedRTMProducer);
        when(coursewareService.getRootElementId(activityId, ACTIVITY)).thenReturn(Mono.just(rootElementId));

        handler = new CreateActivityComponentMessageHandler(activityService,
                                                            pluginService,
                                                            componentService,
                                                            rtmEventBrokerProvider,
                                                            coursewareService,
                                                            authenticationContextProvider,
                                                            rtmClientContextProvider,
                                                            componentCreatedRTMProducer);
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("activityId is required", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noPluginId() {
        when(message.getPluginId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("pluginId is required", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noPLuginVersion() {
        when(message.getPluginVersionExpr()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("pluginVersion is required", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_activityNotFound() {
        TestPublisher<Activity> publisher = TestPublisher.create();
        publisher.error(new ActivityNotFoundException(activityId));

        when(activityService.findById(activityId)).thenReturn(publisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("invalid activity", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_wrongVersionFormat() {
        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));

        TestPublisher<String> versionPublisher = TestPublisher.create();

        versionPublisher.error(new VersionParserFault("unable parse"));

        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(versionPublisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("invalid pluginVersion", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_pluginVersionNotFound() {
        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));

        TestPublisher<String> versionPublisher = TestPublisher.create();

        versionPublisher.error(new PluginNotFoundFault(pluginId));

        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(versionPublisher.mono());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("invalid plugin", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void handle_error() {
        TestPublisher<Component> componentPublisher = TestPublisher.create();
        componentPublisher.error(new ActivityNotFoundException(activityId));

        when(componentService.createForActivity(activityId, pluginId, pluginVersionExpr, config))
                .thenReturn(componentPublisher.mono());

        when(message.getConfig()).thenReturn(config);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.component.create.error\"," +
                "\"code\":400," +
                "\"message\":\"no activity with id " + activityId + "\"," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_withComponentId() {
        when(message.getComponentId()).thenReturn(componentId);
        when(componentService.createForActivity(activityId, pluginId, pluginVersionExpr, null, componentId))
                .thenReturn(Mono.just(new Component().setId(UUID.randomUUID())));

        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(new ComponentPayload()));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, null))
                .thenReturn(componentCreatedRTMProducer);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{}" +
                "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(coursewareService, never()).saveConfigurationFields(any(UUID.class), any(String.class));
    }

    @Test
    void handle_success_withNullConfig() {
        when(componentService.createForActivity(activityId, pluginId, pluginVersionExpr, null))
                .thenReturn(Mono.just(new Component().setId(UUID.randomUUID())));

        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(new ComponentPayload()));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, null))
                .thenReturn(componentCreatedRTMProducer);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{}" +
                "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(coursewareService, never()).saveConfigurationFields(any(UUID.class), any(String.class));
    }

    @Test
    void handle_success_withConfig() {
        when(componentService.createForActivity(activityId, pluginId, pluginVersionExpr, config))
                .thenReturn(Mono.just(new Component().setId(UUID.randomUUID())));

        when(message.getConfig()).thenReturn(config);

        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(new ComponentPayload()));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, null))
                .thenReturn(componentCreatedRTMProducer);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{}" +
                "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), any(CoursewareElementBroadcastMessage.class));
        verify(componentCreatedRTMProducer, atLeastOnce()).buildComponentCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(null));
        verify(componentCreatedRTMProducer, atLeastOnce()).produce();
        verify(coursewareService).saveConfigurationFields(any(UUID.class), eq(config));
    }

    @Test
    void handle_broadcastMessage() {
        when(componentService.createForActivity(activityId, pluginId, pluginVersionExpr, null))
                .thenReturn(Mono.just(new Component().setId(componentId)));

        ComponentPayload payload = ComponentPayload.from(new Component().setId(componentId),
                                                         "some config",
                                                         new PluginSummary().setId(UUID.randomUUID()),
                                                         new ParentByComponent(),
                                                         new CoursewareElementDescription(),
                                                         new ArrayList<>());
        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(payload));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, componentId))
                .thenReturn(componentCreatedRTMProducer);

        handler.handle(session, message);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(activityId, captor.getValue().getParentElement().getElementId());
        assertEquals(ACTIVITY, captor.getValue().getParentElement().getElementType());

        verify(componentCreatedRTMProducer, atLeastOnce()).buildComponentCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(componentId));
        verify(componentCreatedRTMProducer, atLeastOnce()).produce();
    }
}
