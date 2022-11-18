package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.message.handler.courseware.activity.ReplaceActivityConfigMessageHandler.AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.ActivityEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.ReplaceActivityConfigMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.ActivityConfigChangeRTMProducer;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ReplaceActivityConfigMessageHandlerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private CamelReactiveStreamsService camel;

    @Mock
    private ActivityConfigChangeRTMProducer activityConfigChangeRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private ReplaceActivityConfigMessageHandler handler;
    private Provider<AuthenticationContext> authenticationContextProvider;
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    private Session session;
    private RTMEventBroker rtmEventBroker;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final String config = "{\"foo\", \"bar\"}";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthenticationContext authenticationContext = RTMWebSocketTestUtils.mockAuthenticationContext(accountId);
        rtmEventBroker = mock(RTMEventBroker.class);
        authenticationContextProvider = mock(Provider.class);
        rtmEventBrokerProvider = mock(Provider.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        session = RTMWebSocketTestUtils.mockSession();

        TestPublisher<Exchange> exchangePublisher = TestPublisher.create();

        when(camel.toStream(anyString(), any(ActivityEventMessage.class)))
                .thenReturn(exchangePublisher);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(activityConfigChangeRTMProducer.buildActivityConfigChangeRTMConsumable(rtmClientContext, rootElementId, activityId, config))
                .thenReturn(activityConfigChangeRTMProducer);
        when(coursewareService.getRootElementId(activityId, ACTIVITY)).thenReturn(Mono.just(rootElementId));
        when(coursewareService.saveConfigurationFields(activityId, config)).thenReturn(Flux.just(new Void[]{}));

        handler = new ReplaceActivityConfigMessageHandler(authenticationContextProvider,
                                                          activityService,
                                                          rtmEventBrokerProvider,
                                                          coursewareService,
                                                          rtmClientContextProvider,
                                                          activityConfigChangeRTMProducer);
    }

    @Test
    void validate_noActivityId() {
        ReplaceActivityConfigMessage message = mock(ReplaceActivityConfigMessage.class);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing activityId", ex.getErrorMessage());
    }

    @Test
    void validate_noConfig() {
        ReplaceActivityConfigMessage message = mock(ReplaceActivityConfigMessage.class);
        when(message.getActivityId()).thenReturn(activityId);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing config", ex.getErrorMessage());
    }

    @Test
    void validate_activityNotFound() {
        ReplaceActivityConfigMessage message = mock(ReplaceActivityConfigMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getConfig()).thenReturn(config);
        when(activityService.findById(message.getActivityId())).thenThrow(new ActivityNotFoundException(activityId));

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("invalid activityId", ex.getErrorMessage());
    }

    @Test
    void handle() throws IOException {
        ReplaceActivityConfigMessage message = mock(ReplaceActivityConfigMessage.class);
        when(message.getType()).thenReturn("author.activity.config.replace");
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getConfig()).thenReturn(config);
        when(activityService.replaceConfig(accountId, activityId, config)).thenReturn(Mono.empty());

        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       new ActivityConfig().setConfig(config).setId(UUIDs.timeBased()),
                                                       new PluginSummary().setId(UUID.randomUUID()),
                                                       new AccountPayload().setAccountId(accountId),
                                                       new ActivityTheme().setId(UUID.randomUUID()),
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activityId))).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        verify(coursewareService).saveConfigurationFields(activityId, config);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("author.activity.config.replace.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("activity"));
                assertEquals(activityId.toString(), responseMap.get("activityId"));
                assertEquals(config, responseMap.get("config"));
                assertEquals(payload.getPlugin().getPluginId().toString(),
                             ((Map) responseMap.get("plugin")).get("pluginId"));
                assertEquals(accountId.toString(), ((Map) responseMap.get("creator")).get("accountId"));
                assertNotNull(responseMap.get("createdAt"));
                assertNotNull(responseMap.get("updatedAt"));
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(
                CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq("author.activity.config.replace"), captor.capture());
        assertEquals(CoursewareAction.CONFIG_CHANGE, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getElement().getElementType());
        assertEquals(activityId, captor.getValue().getElement().getElementId());
        assertNull(captor.getValue().getParentElement());

        verify(activityConfigChangeRTMProducer, atLeastOnce()).buildActivityConfigChangeRTMConsumable(eq(
                rtmClientContext), eq(rootElementId), eq(activityId), eq(config));
        verify(activityConfigChangeRTMProducer, atLeastOnce()).produce();
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_exception() throws IOException {
        ReplaceActivityConfigMessage message = mock(ReplaceActivityConfigMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getConfig()).thenReturn("config");
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(activityService.replaceConfig(eq(accountId), eq(activityId), eq("config"))).thenReturn(mono);
        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       new ActivityConfig().setConfig("config").setId(UUIDs.timeBased()),
                                                       new PluginSummary().setId(UUID.randomUUID()),
                                                       new AccountPayload().setAccountId(accountId),
                                                       new ActivityTheme().setId(UUID.randomUUID()).setActivityId(
                                                               activityId),
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activityId))).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"author.activity.config.replace.error\",\"code\":422," +
                                                          "\"message\":\"Unable to replace config\"}");
    }

    @Test
    void handle_noPayload() throws IOException {
        ReplaceActivityConfigMessage message = mock(ReplaceActivityConfigMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getConfig()).thenReturn("{\"foo\", \"bar\"}");
        when(activityService.replaceConfig(eq(accountId), eq(activityId), eq("{\"foo\", \"bar\"}"))).thenReturn(Mono.empty());
        when(activityService.getActivityPayload(eq(activityId))).thenReturn(Mono.empty());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"author.activity.config.replace.ok\",\"code\":202}");
    }
}
