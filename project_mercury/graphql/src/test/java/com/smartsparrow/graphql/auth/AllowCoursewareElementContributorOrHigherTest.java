package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AllowCoursewareElementContributorOrHigherTest {

    @InjectMocks
    private AllowCoursewareElementContributorOrHigher authorizer;

    @Mock
    private CoursewareService coursewareService;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private ProjectPermissionService projectPermissionService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account account = mock(Account.class);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
    }

    @Test
    void test_success_forContributor() {
        when(coursewareService.getWorkspaceId(elementId, elementType)).thenReturn(Mono.just(workspaceId));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(projectId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        boolean result = authorizer.test(authenticationContext,elementId, elementType);

        assertTrue(result);
    }

    @Test
    void test_success_forOwner() {
        when(coursewareService.getWorkspaceId(elementId, elementType)).thenReturn(Mono.just(workspaceId));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(projectId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.OWNER));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        boolean result = authorizer.test(authenticationContext,elementId, elementType);

        assertTrue(result);
    }

    @Test
    void test_noAccess_forReviewer() {
        when(coursewareService.getWorkspaceId(elementId, elementType)).thenReturn(Mono.just(workspaceId));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(projectId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));


        boolean result = authorizer.test(authenticationContext,elementId, elementType);

        assertFalse(result);
    }


    @Test
    void test_noPermission() {
        when(coursewareService.getWorkspaceId(elementId, elementType)).thenReturn(Mono.just(workspaceId));
        when(coursewareService.getProjectId(elementId, elementType)).thenReturn(Mono.just(projectId));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.empty());
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.empty());

        boolean result = authorizer.test(authenticationContext,elementId, elementType);

        assertFalse(result);
    }

    @Test
    void test_exception() {
        TestPublisher<UUID> workspacePublisher = TestPublisher.create();
        workspacePublisher.error(new RuntimeException("any runtime exception"));
        when(coursewareService.getWorkspaceId(elementId, elementType)).thenReturn(workspacePublisher.mono());

        boolean result = authorizer.test(authenticationContext,elementId, elementType);

        assertFalse(result);
    }
}
