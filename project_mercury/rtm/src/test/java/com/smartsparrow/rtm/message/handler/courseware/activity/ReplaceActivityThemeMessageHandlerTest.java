package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.ReplaceActivityThemeMessage;
import com.smartsparrow.rtm.subscription.courseware.themechange.ActivityThemeChangeRTMProducer;

import reactor.core.publisher.Mono;

class ReplaceActivityThemeMessageHandlerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private ActivityThemeChangeRTMProducer activityThemeChangeRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private ReplaceActivityThemeMessageHandler replaceActivityThemeMessageHandler;
    private final UUID accountId = UUID.randomUUID();
    private final UUID activityThemeId = UUID.randomUUID();
    private final UUID activityId = UUID.randomUUID();
    private final UUID rootElementId = UUID.randomUUID();
    private static final String config = "my awesome config";
    private Session session;
    private RTMEventBroker rtmEventBroker;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        rtmEventBroker = mock(RTMEventBroker.class);
        rtmEventBrokerProvider = mock(Provider.class);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(activityId, ACTIVITY)).thenReturn(Mono.just(rootElementId));
        when(activityThemeChangeRTMProducer.buildActivityThemeChangeRTMConsumable(rtmClientContext, rootElementId, activityId, config))
                .thenReturn(activityThemeChangeRTMProducer);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        replaceActivityThemeMessageHandler = new ReplaceActivityThemeMessageHandler(
                activityService,
                coursewareService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                activityThemeChangeRTMProducer
        );
    }

    @Test
    void validate_missingActivityId() {
        ReplaceActivityThemeMessage message = mock(ReplaceActivityThemeMessage.class);
        RTMValidationException e = assertThrows(RTMValidationException.class,
                () -> replaceActivityThemeMessageHandler.validate(message));

        assertEquals("activity id is required", e.getErrorMessage());
    }

    @Test
    void validate_EmptyConfig() throws RTMValidationException {
        ReplaceActivityThemeMessage message = mock(ReplaceActivityThemeMessage.class);
        when(message.getActivityId()).thenReturn(UUID.randomUUID());
        replaceActivityThemeMessageHandler.validate(message);
    }

    @Test
    void validate_WithConfig() throws RTMValidationException {
        ReplaceActivityThemeMessage message = mock(ReplaceActivityThemeMessage.class);
        when(message.getActivityId()).thenReturn(UUID.randomUUID());
        when(message.getConfig()).thenReturn(config);
        replaceActivityThemeMessageHandler.validate(message);
    }

    @Test
    void handle_success() throws Exception {
        ActivityTheme activityTheme = new ActivityTheme()
                .setId(activityThemeId)
                .setActivityId(activityId)
                .setConfig(config);

        ReplaceActivityThemeMessage message = mock(ReplaceActivityThemeMessage.class);
        when(message.getType()).thenReturn("type");
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getConfig()).thenReturn(config);
        when(activityService.replaceActivityThemeConfig(any(), any())).thenReturn(Mono.just(activityTheme));
        replaceActivityThemeMessageHandler.handle(session, message);
        verifySentMessage(session, "{\"type\":\"author.activity.theme.replace.ok\"," +
                "\"response\":{\"activityTheme\":" +
                "{\"id\":\"" + activityThemeId + "\"," +
                "\"activityId\":\"" + activityId + "\"," +
                "\"config\":\"" + config + "\"}}}");

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.THEME_CHANGE, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getElement().getElementType());
        assertEquals(activityId, captor.getValue().getElement().getElementId());
        assertNull(captor.getValue().getParentElement());

        verify(activityThemeChangeRTMProducer, atLeastOnce()).buildActivityThemeChangeRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(activityId), eq(config));
        verify(activityThemeChangeRTMProducer, atLeastOnce()).produce();
    }

}
