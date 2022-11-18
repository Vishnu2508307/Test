package com.smartsparrow.rtm.message.authorization;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AllowCoursewareElementReviewerOrHigherTest {

    @InjectMocks
    private AllowCoursewareElementReviewerOrHigher authorizer;

    @Mock
    private CoursewareService coursewareService;
    @Mock
    private WorkspaceService workspaceService;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID workspaceId = UUID.randomUUID();

    @Mock
    private CoursewareElementMessage message;
    @Mock
    private CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(coursewareElementAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    void test_success_forContributor() {
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(workspaceId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));


        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_success_forOwner() {
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(workspaceId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_noAccess_forReviewer() {
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(workspaceId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));


        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_workspaceNotFound() {
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.empty());

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);//TODO remove when PLT-4284 is done
//        assertFalse(result);
    }

    @Test
    void test_noPermission() {
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(workspaceId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.empty());
        when(coursewareElementAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }

    @Test
    void test_exception() {
        TestPublisher<UUID> workspacePublisher = TestPublisher.create();
        workspacePublisher.error(new RuntimeException("any exception"));
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(workspacePublisher.mono());
        when(coursewareElementAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }

}
