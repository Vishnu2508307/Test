package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.DeletedActivity;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.IamTestUtils;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.workspace.DeleteProjectActivityMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class DeleteProjectActivityMessageHandlerTest {

    @InjectMocks
    private DeleteProjectActivityMessageHandler handler;

    @Mock
    private ActivityService activityService;

    @Mock
    private DeleteProjectActivityMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    private static final UUID projectId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        IamTestUtils.mockAuthenticationContextProvider(authenticationContextProvider, accountId);
        when(message.getProjectId()).thenReturn(projectId);
        when(message.getActivityId()).thenReturn(activityId);
    }


    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("projectId is required", f.getMessage());
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void validate_activityNotFound() {
        when(activityService.findById(message.getActivityId())).thenThrow(ActivityNotFoundException.class);

        NotFoundFault t = assertThrows(NotFoundFault.class, () -> handler.validate(message));
        assertEquals("Activity not found", t.getMessage());
        assertEquals("NOT_FOUND", t.getType());
        assertEquals(404, t.getResponseStatusCode());
    }

    @Test
    void validate_deletedActivity() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        when(activityService.fetchDeletedActivityById(message.getActivityId())).thenReturn(Mono.just(new DeletedActivity()));

        NotFoundFault t = assertThrows(NotFoundFault.class, () -> handler.validate(message));
        assertEquals("Activity not found", t.getMessage());
        assertEquals("NOT_FOUND", t.getType());
        assertEquals(404, t.getResponseStatusCode());
    }

    @Test
    void validate_success() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        when(activityService.fetchDeletedActivityById(message.getActivityId())).thenReturn(Mono.empty());
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(activityService.deleteFromProject(activityId, projectId, accountId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.delete.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        assertEquals(CoursewareAction.DELETED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.ACTIVITY, captor.getValue().getElement().getElementType());
        assertEquals(activityId, captor.getValue().getElement().getElementId());
        assertEquals(projectId, captor.getValue().getProjectId());
        assertNull(captor.getValue().getParentElement());
    }

    @Test
    void handle_error() throws WriteResponseException {
        when(activityService.deleteFromProject(activityId, projectId, accountId)).thenReturn(Flux.error(new RuntimeException("black hole")));

        handler.handle(session, message);
        String expected = "{\"type\":\"project.activity.delete.error\",\"code\":422,\"message\":\"error deleting the root activity\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmEventBroker, never())
                .broadcast(eq(message.getType()), any(CoursewareElementBroadcastMessage.class));
    }
}
