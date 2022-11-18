package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.courseware.pathway.PathwayMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

class AllowPathwayContributorOrHigherTest {

    @Mock
    private CoursewareService coursewareService;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private PathwayMessage pathwayMessage;
    @Mock
    private ProjectPermissionService projectPermissionService;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;
    @InjectMocks
    private AllowPathwayContributorOrHigher authorizer;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(pathwayMessage.getPathwayId()).thenReturn(pathwayId);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        // simulate always fall-back
        when(coursewareService.getProjectId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(projectId));
    }

    @Test
    void test_allowOwner() {
        when(coursewareService.getWorkspaceId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(workspaceId));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(authorizer.test(authenticationContext, pathwayMessage));
    }

    @Test
    void test_allowContributor() {
        when(coursewareService.getWorkspaceId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(workspaceId));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, pathwayMessage));
    }

    @Test
    void test_allowProjectContributor() {
        when(coursewareService.getProjectId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(projectId));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(coursewareService.getWorkspaceId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, pathwayMessage));
    }

    @Test
    void test_notAllowReviewer() {
        when(coursewareService.getWorkspaceId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(workspaceId));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(authorizer.test(authenticationContext, pathwayMessage));
    }

    @Test
    void test_noWorkspace() {
        when(coursewareService.getWorkspaceId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, pathwayMessage));
    }

    @Test
    void test_noPermission() {
        when(coursewareService.getWorkspaceId(pathwayId, CoursewareElementType.PATHWAY)).thenReturn(Mono.just(workspaceId));
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, pathwayMessage));
    }
}
