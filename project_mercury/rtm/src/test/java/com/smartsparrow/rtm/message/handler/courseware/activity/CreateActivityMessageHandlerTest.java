package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.CreateActivityMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ActivityCreatedRTMProducer;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class CreateActivityMessageHandlerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    Account account;

    @Mock
    private ActivityCreatedRTMProducer activityCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    CreateActivityMessageHandler handler;


    private Provider<AuthenticationContext> authenticationContextProvider;
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    private RTMEventBroker rtmEventBroker;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final String config = "some config";
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(parentPathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));
        when(activityCreatedRTMProducer.buildActivityCreatedRTMConsumable(rtmClientContext, rootElementId, activityId, parentPathwayId))
                .thenReturn(activityCreatedRTMProducer);

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        Tuple2<Provider<AuthenticationContext>, AuthenticationContext> mockAuthenticationContextProvider = RTMWebSocketTestUtils
                .mockProvidedClass(AuthenticationContext.class);

        authenticationContextProvider = mockAuthenticationContextProvider.getT1();
        AuthenticationContext authenticationContext = mockAuthenticationContextProvider.getT2();

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        session = RTMWebSocketTestUtils.mockSession();

        handler = new CreateActivityMessageHandler(authenticationContextProvider,
                                                   activityService,
                                                   pathwayService,
                                                   rtmEventBrokerProvider,
                                                   coursewareService,
                                                   rtmClientContextProvider,
                                                   activityCreatedRTMProducer);

        when(coursewareService.saveConfigurationFields(any(UUID.class), anyString())).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void handle_success() throws IOException {
        Activity activity = new Activity() //
                .setId(activityId) //
                .setPluginId(pluginId) //
                .setPluginVersionExpr("1.0.0");
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.0.0");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(message.getActivityId()).thenReturn(activityId);
        when(activityService.create(accountId,
                                    message.getPluginId(),
                                    message.getParentPathwayId(),
                                    message.getPluginVersionExpr(),
                                    message.getActivityId()))
                .thenReturn(Mono.just(activity));
        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       new ActivityConfig(),
                                                       new PluginSummary(),
                                                       new AccountPayload(),
                                                       new ActivityTheme(),
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activity))).thenReturn(Mono.just(payload));

        handler.handle(session, message);
        verify(activityService).create(eq(accountId), eq(pluginId), eq(parentPathwayId), eq("1.0.0"), eq(activityId));
        verify(activityService).getActivityPayload(eq(activity));

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(
                CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getElement().getElementType());
        assertEquals(activityId, captor.getValue().getElement().getElementId());
        assertNotNull(captor.getValue().getParentElement());

        verify(activityCreatedRTMProducer, atLeastOnce()).buildActivityCreatedRTMConsumable(eq(rtmClientContext),
                                                                                            eq(rootElementId),
                                                                                            eq(activityId),
                                                                                            eq(parentPathwayId));
        verify(activityCreatedRTMProducer, atLeastOnce()).produce();

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("author.activity.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(activityId.toString(), responseMap.get("activityId"));
            assertNotNull(responseMap.get("createdAt"));
        }));
    }

    @Test
    void handle_successWithConfig() throws IOException {
        ActivityConfig activityConfig = new ActivityConfig()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setConfig(config);

        Activity activity = new Activity() //
                .setId(activityId) //
                .setPluginId(pluginId) //
                .setPluginVersionExpr("1.0.0");
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.0.0");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(message.getConfig()).thenReturn(config);
        when(message.getActivityId()).thenReturn(activityId);
        when(activityService.create(accountId,
                                    message.getPluginId(),
                                    message.getParentPathwayId(),
                                    message.getPluginVersionExpr(),
                                    message.getActivityId()))
                .thenReturn(Mono.just(activity));
        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       activityConfig,
                                                       new PluginSummary(),
                                                       new AccountPayload(),
                                                       new ActivityTheme(),
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activity))).thenReturn(Mono.just(payload));
        when(activityService.replaceConfig(any(UUID.class),
                                           any(UUID.class),
                                           any(String.class))).thenReturn(Mono.empty());

        handler.handle(session, message);
        verify(activityService).create(eq(accountId), eq(pluginId), eq(parentPathwayId), eq("1.0.0"), eq(activityId));
        verify(activityService).getActivityPayload(eq(activity));
        verify(activityService, atLeastOnce()).replaceConfig(any(), any(), any());

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("author.activity.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(activityId.toString(), responseMap.get("activityId"));
            assertNotNull(responseMap.get("createdAt"));
            assertNotNull(responseMap.get("config"));
        }));
    }

    @Test
    void handle_successWithTheme() throws IOException {
        ActivityTheme activityTheme = new ActivityTheme()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setConfig(config);

        Activity activity = new Activity() //
                .setId(activityId) //
                .setPluginId(pluginId) //
                .setPluginVersionExpr("1.0.0");
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.0.0");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(message.getTheme()).thenReturn(config);
        when(message.getActivityId()).thenReturn(activityId);

        when(activityService.create(accountId,
                                    message.getPluginId(),
                                    message.getParentPathwayId(),
                                    message.getPluginVersionExpr(),
                                    message.getActivityId()))
                .thenReturn(Mono.just(activity));

        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       new ActivityConfig(),
                                                       new PluginSummary(),
                                                       new AccountPayload(),
                                                       activityTheme,
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activity))).thenReturn(Mono.just(payload));
        when(activityService.replaceActivityThemeConfig(any(UUID.class), any(String.class))).thenReturn(Mono.just(
                activityTheme));

        handler.handle(session, message);
        verify(activityService).create(eq(accountId), eq(pluginId), eq(parentPathwayId), eq("1.0.0"), eq(activityId));
        verify(activityService).getActivityPayload(eq(activity));
        verify(activityService, atLeastOnce()).replaceActivityThemeConfig(any(), any());

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("author.activity.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(activityId.toString(), responseMap.get("activityId"));
            assertNotNull(responseMap.get("createdAt"));
            assertNotNull(responseMap.get("activityTheme"));
        }));
    }

    @Test
    void handle_successWithConfigAndTheme() throws IOException {
        ActivityConfig activityConfig = new ActivityConfig()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setConfig(config);

        ActivityTheme activityTheme = new ActivityTheme()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setConfig(config);

        Activity activity = new Activity() //
                .setId(activityId) //
                .setPluginId(pluginId) //
                .setPluginVersionExpr("1.0.0");
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.0.0");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(message.getConfig()).thenReturn(config);
        when(message.getTheme()).thenReturn(config);
        when(message.getActivityId()).thenReturn(activityId);

        when(activityService.create(accountId,
                                    message.getPluginId(),
                                    message.getParentPathwayId(),
                                    message.getPluginVersionExpr(),
                                    message.getActivityId()))
                .thenReturn(Mono.just(activity));

        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       activityConfig,
                                                       new PluginSummary(),
                                                       new AccountPayload(),
                                                       activityTheme,
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activity))).thenReturn(Mono.just(payload));
        when(activityService.replaceActivityThemeConfig(any(UUID.class), any(String.class))).thenReturn(Mono.just(
                activityTheme));
        when(activityService.replaceConfig(any(UUID.class),
                                           any(UUID.class),
                                           any(String.class))).thenReturn(Mono.empty());

        handler.handle(session, message);
        verify(activityService).create(eq(accountId), eq(pluginId), eq(parentPathwayId), eq("1.0.0"), eq(activityId));
        verify(activityService).getActivityPayload(eq(activity));
        verify(activityService, atLeastOnce()).replaceActivityThemeConfig(any(), any());
        verify(activityService, atLeastOnce()).replaceConfig(any(), any(), any());

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("author.activity.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(activityId.toString(), responseMap.get("activityId"));
            assertNotNull(responseMap.get("createdAt"));
            assertNotNull(responseMap.get("config"));
            assertNotNull(responseMap.get("activityTheme"));
        }));
    }

    @Test
    void handle_pluginNotFoundException() throws IOException {
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("2.0.0");

        // Prep blowing publisher
        TestPublisher<Activity> riggedPublisher = TestPublisher.create();
        when(activityService.create(any(), any(), any(), any(String.class), any())).thenReturn(riggedPublisher.mono());
        String errorMsg = String.format("Plugin not found");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        // let it subscribe
        handler.handle(session, message);
        riggedPublisher.assertSubscribers();

        // boom
        riggedPublisher.error(new PluginNotFoundFault((errorMsg)));

        verifySentMessage(session,
                "{\"type\":\"author.activity.create.error\",\"code\":404,\"message\":\"" + errorMsg + "\"}");
        verify(activityCreatedRTMProducer, never()).produce();
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_pluginVersionParseException() throws IOException {
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("lol nope");

        Mono mono = TestPublisher.create().error(new VersionParserFault("unable parse")).mono();
        when(activityService.create(any(), any(), any(), any(String.class), any())).thenReturn(mono);
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);

        handler.handle(session, message);
        verifySentMessage(session,
                "{\"type\":\"author.activity.create.error\",\"code\":400,\"message\":\"Unable to parse version expression 'lol nope'\"}");
    }

    @Test
    void validate_success() throws IllegalArgumentFault {
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.0.0");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        Pathway pathway = mockPathway(parentPathwayId);
        when(pathwayService.findById(parentPathwayId)).thenReturn(Mono.just(pathway));
        handler.validate(message);
    }

    @Test
    void validate_noPlugin() {
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                                                () -> handler.validate(message));
        assertEquals("missing plugin id parameter", t.getMessage());
    }

    @Test
    void validate_noPluginVersion() {
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> handler.validate(message));
        assertEquals("plugin version expression required", t.getMessage());
    }

    @Test
    void validate_noparentPathwayId() {
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.0.0");
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                                              () -> handler.validate(message));
        assertEquals("parentPathwayId is required", t.getMessage());
    }

    @Test
    void validate_parentPathwayIdNotFound() {
        UUID parentPathwayId = UUID.randomUUID();
        CreateActivityMessage message = mock(CreateActivityMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn("1.*");
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);

        when(pathwayService.findById(parentPathwayId)).thenReturn(Mono.empty());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));
        assertEquals(String.format("parentPathwayId `%s` not found", parentPathwayId), t.getMessage());
    }

}
