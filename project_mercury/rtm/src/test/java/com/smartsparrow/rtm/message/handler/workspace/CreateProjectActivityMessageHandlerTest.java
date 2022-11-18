package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.rtm.message.handler.workspace.CreateProjectActivityMessageHandler.PROJECT_ACTIVITY_CREATE;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.workspace.CreateProjectActivityMessage;
import com.smartsparrow.workspace.data.ActivityThemeIconLibrary;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateProjectActivityMessageHandlerTest {

    private CreateProjectActivityMessageHandler handler;

    @Mock
    private ActivityService activityService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private CreateProjectActivityMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID activityId = UUIDs.timeBased();
    private static final String pluginVersionExpr = "*.1";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn(pluginVersionExpr);
        when(message.getActivityId()).thenReturn(null);

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        handler = new CreateProjectActivityMessageHandler(
                authenticationContextProvider,
                activityService,
                coursewareService,
                rtmEventBrokerProvider
        );
    }

    @Test
    void validate_noPluginId() {
        when(message.getPluginId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("pluginId is required", fault.getMessage());
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("projectId is required", fault.getMessage());
    }

    @Test
    void validate_noPluginVersionExpression() {
        when(message.getPluginVersionExpr()).thenReturn("");

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("pluginVersionExpr is required", fault.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(activityService.getActivityPayload(activityId)).thenReturn(Mono.just(new ActivityPayload()));
        when(activityService.create(accountId, pluginId, pluginVersionExpr, null))
                .thenReturn(Mono.just(new Activity().setId(activityId)));

        TestPublisher<Void> addToProject = TestPublisher.create();
        addToProject.complete();

        when(activityService.addToProject(activityId, projectId)).thenReturn(addToProject.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.create.ok\",\"response\":{\"activity\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_withConfig() throws WriteResponseException {
        when(activityService.getActivityPayload(activityId)).thenReturn(Mono.just(new ActivityPayload()));
        when(message.getConfig()).thenReturn("{\"foo\":\"bar\"}");

        when(activityService.create(accountId, pluginId, pluginVersionExpr, null))
                .thenReturn(Mono.just(new Activity().setId(activityId)));

        TestPublisher<Void> addToProject = TestPublisher.create();
        addToProject.complete();

        TestPublisher<Void> config = TestPublisher.create();
        config.complete();

        when(activityService.replaceConfig(accountId, activityId, message.getConfig()))
                .thenReturn(config.mono());
        when(coursewareService.saveConfigurationFields(activityId, message.getConfig()))
                .thenReturn(Flux.just(new Void[]{}));
        when(activityService.addToProject(activityId, projectId)).thenReturn(addToProject.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.create.ok\",\"response\":{\"activity\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_broadcastMessage() throws WriteResponseException {
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        when(activityService.getActivityPayload(activityId)).thenReturn(Mono.just(
                ActivityPayload.from(new Activity().setId(activityId), new ActivityConfig(), new PluginSummary(),
                                     new AccountPayload(), new ActivityTheme(), new ArrayList<>(), new ArrayList<>(),
                                     new CoursewareElementDescription(), pluginFilterList, new ThemePayload(), Collections.emptyList())));

        when(activityService.create(accountId, pluginId, pluginVersionExpr, null))
                .thenReturn(Mono.just(new Activity().setId(activityId)));

        TestPublisher<Void> addToProject = TestPublisher.create();
        addToProject.complete();

        when(activityService.addToProject(activityId, projectId)).thenReturn(addToProject.mono());

        handler.handle(session, message);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(PROJECT_ACTIVITY_CREATE), captor.capture());
        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getElement().getElementType());
        assertEquals(activityId, captor.getValue().getElement().getElementId());
        assertNull(captor.getValue().getParentElement());
    }
}
