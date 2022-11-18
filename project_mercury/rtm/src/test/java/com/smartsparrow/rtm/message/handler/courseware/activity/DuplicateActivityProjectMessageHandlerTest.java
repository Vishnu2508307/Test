package com.smartsparrow.rtm.message.handler.courseware.activity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.activity.DuplicateActivityProjectMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DuplicateActivityProjectMessageHandlerTest {

    @InjectMocks
    private DuplicateActivityProjectMessageHandler handler;

    @Mock
    private ActivityService activityService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private DuplicateActivityProjectMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID newActivityId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private static final Account account = new Account().setId(accountId).setSubscriptionId(UUID.randomUUID());
    private Boolean newDuplicateFlow = false;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);

        when(message.getActivityId()).thenReturn(activityId);
        when(message.getProjectId()).thenReturn(projectId);

        TestPublisher<Void> addToProject = TestPublisher.create();
        addToProject.complete();

        when(activityService.isDuplicatedCourseInTheSameProject(activityId, projectId, newDuplicateFlow)).thenReturn(Mono.just(false));

        when(coursewareService.duplicateActivity(eq(activityId), eq(account), any(Boolean.class)))
                .thenReturn(Mono.just(new Activity().setId(newActivityId)));

        when(activityService.addToProject(newActivityId, projectId))
                .thenReturn(Mono.empty());
        when(activityService.getActivityPayload(any(Activity.class)))
                .thenReturn(Mono.just(new ActivityPayload()));
    }

    @Test
    void validate_noActivity() {
        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void validate_noProject() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("projectId is required", f.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_duplicateError() throws WriteResponseException {
        TestPublisher<Activity> publisher = TestPublisher.create();
        publisher.error(new RuntimeException());
        when(coursewareService.duplicateActivity(activityId, account, false))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.activity.duplicate.error\"," +
                "\"code\":422,\"message\":\"Unable to duplicate activity\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_addToProjectError() throws WriteResponseException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException());
        when(activityService.addToProject(newActivityId, projectId))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                        "\"type\":\"project.activity.duplicate.error\"," +
                        "\"code\":422,\"message\":\"Unable to duplicate activity\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_payloadError() throws WriteResponseException {
        TestPublisher<ActivityPayload> publisher = TestPublisher.create();
        publisher.error(new RuntimeException());
        when(activityService.getActivityPayload(any(Activity.class)))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.activity.duplicate.error\"," +
                "\"code\":422,\"message\":\"Unable to duplicate activity\"}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.duplicate.ok\",\"response\":{\"activity\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_inDifferentProject_withNewDuplicateFlow() throws WriteResponseException {
        newDuplicateFlow = true;

        when(activityService.isDuplicatedCourseInTheSameProject(activityId, projectId, newDuplicateFlow)).thenReturn(Mono.just(true));

        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.duplicate.ok\",\"response\":{\"activity\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_inSameProject_withNewDuplicateFlow() throws WriteResponseException {
        newDuplicateFlow = true;

        when(activityService.isDuplicatedCourseInTheSameProject(activityId, projectId, newDuplicateFlow)).thenReturn(Mono.just(false));

        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.duplicate.ok\",\"response\":{\"activity\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}