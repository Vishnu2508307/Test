package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.activity.MoveActivityMessageHandler.AUTHOR_ACTIVITY_MOVE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.activity.MoveActivityMessageHandler.AUTHOR_ACTIVITY_MOVE_OK;
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

import javax.inject.Provider;

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
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.MoveActivityMessage;
import com.smartsparrow.rtm.subscription.courseware.moved.ActivityMovedRTMProducer;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class MoveActivityMessageHandlerTest {

    @Mock
    private ActivityService activityService;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private MoveActivityMessage message;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private ActivityMovedRTMProducer activityMovedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    private MoveActivityMessageHandler handler;
    private Session session;

    private static final UUID rootElementId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID oldParentPathwayId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final int index = 1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = mock(Account.class);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getIndex()).thenReturn(index);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(pathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        handler = new MoveActivityMessageHandler(
                activityService,
                coursewareService,
                authenticationContextProvider,
                rtmEventBrokerProvider,
                rtmClientContextProvider,
                activityMovedRTMProducer);

        when(activityService.findParentPathwayId(message.getActivityId())).thenReturn(Mono.just(oldParentPathwayId));
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void validate_activityNotFound() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.empty());

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("Activity not found", f.getMessage());
    }

    @Test
    void validate_noDestinationPathwayId() {
        when(message.getPathwayId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("pathwayId is required", f.getMessage());
    }

    @Test
    void validate_rootActivityId() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        when(activityService.findParentPathwayId(message.getActivityId())).thenReturn(Mono.empty());

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("Only child activities can be moved. Please use project.activity.move for root activity", f.getMessage());
    }

    @Test
    void validate_negativeIndex() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        when(message.getIndex()).thenReturn(-1);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("index should be >= 0", f.getMessage());
    }

    @Test
    void validate() throws RTMValidationException {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        when(activityService.findParentPathwayId(message.getActivityId())).thenReturn(Mono.just(parentPathwayId));

        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(
                CoursewareElementBroadcastMessage.class);
        Activity newActivity = new Activity().setId(activityId);
        ActivityPayload expectedPayload = ActivityPayload.from(newActivity,
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
        expectedPayload.setParentPathwayId(pathwayId);
        when(activityService.move(activityId, pathwayId, index, oldParentPathwayId)).thenReturn(Mono.just(
                expectedPayload));
        when(activityService.getActivityPayload(activityId)).thenReturn(Mono.just(expectedPayload));
        when(activityMovedRTMProducer.buildActivityMovedRTMConsumable(rtmClientContext,
                                                                      rootElementId,
                                                                      activityId,
                                                                      oldParentPathwayId,
                                                                      pathwayId))
                .thenReturn(activityMovedRTMProducer);

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_ACTIVITY_MOVE_OK, response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(activityId.toString(), responseMap.get("activityId"));
        }));

        verify(rtmEventBroker).broadcast(eq("author.activity.move"), captor.capture());

        final CoursewareElementBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(CoursewareElement.from(oldParentPathwayId, CoursewareElementType.PATHWAY),
                     broadcastMessage.getOldParentElement());
        assertEquals(CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
                     broadcastMessage.getParentElement());
        assertEquals(CoursewareAction.ACTIVITY_MOVED, broadcastMessage.getAction());
        assertEquals(CoursewareElement.from(activityId, CoursewareElementType.ACTIVITY), broadcastMessage.getElement());

        verify(activityMovedRTMProducer, atLeastOnce()).buildActivityMovedRTMConsumable(eq(rtmClientContext),
                                                                                        eq(rootElementId),
                                                                                        eq(activityId),
                                                                                        eq(oldParentPathwayId),
                                                                                        eq(pathwayId));
        verify(activityMovedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ActivityPayload> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(activityService.move(activityId, pathwayId, index, oldParentPathwayId)).thenReturn(error.mono());
        when(activityService.getActivityPayload(any(Activity.class))).thenReturn(Mono.empty());

        handler.handle(session, message);

        verifySentMessage(session,
                          "{\"type\":\"" + AUTHOR_ACTIVITY_MOVE_ERROR + "\",\"code\":422,\"message\":\"Unable to move activity\"}");

        verify(rtmEventBroker, never()).broadcast(anyString(), any(CoursewareElementBroadcastMessage.class));
        verify(activityMovedRTMProducer, never()).produce();
    }

    @Test
    void handle_activityNotFound() throws IOException {
        TestPublisher<ActivityPayload> error = TestPublisher.create();
        error.error(new ActivityNotFoundException(activityId));
        when(activityService.move(activityId, pathwayId, index, oldParentPathwayId)).thenReturn(error.mono());
        when(activityService.getActivityPayload(activityId)).thenReturn(Mono.empty());

        handler.handle(session, message);

        verifySentMessage(session,
                          "{\"type\":\"" + AUTHOR_ACTIVITY_MOVE_ERROR + "\",\"code\":404,\"message\":\"Activity not found\"}");

        verify(rtmEventBroker, never()).broadcast(anyString(), any(CoursewareElementBroadcastMessage.class));
        verify(activityMovedRTMProducer, never()).produce();
    }
}
