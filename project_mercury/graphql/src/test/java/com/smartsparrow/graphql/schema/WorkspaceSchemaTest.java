package com.smartsparrow.graphql.schema;

import static graphql.Assert.assertNotNull;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.auth.AllowWorkspaceRoles;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

public class WorkspaceSchemaTest {

    @InjectMocks
    WorkspaceSchema workspaceDocumentSchema;

    @Mock
    WorkspaceService workspaceService;

    @Mock
    AllowWorkspaceRoles allowWorkspaceRoles;

    @Mock
    AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    private ResolutionEnvironment resolutionEnvironment;

    private UUID workspaceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Account account = new Account()
                .setId(UUID.randomUUID());

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContext)).build(),
                null,
                null,
                null,
                null);

    }

    @Test
    void getWorkspaceDocuments_AccountIsNull() {
        when(authenticationContext.getAccount()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> workspaceDocumentSchema.getWorkspace(resolutionEnvironment, null));
        assertEquals("account cannot be null", e.getMessage());
    }

    @Test
    void getWorkspaceDocuments_WorkspaceIdIsNull() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> workspaceDocumentSchema.getWorkspace(resolutionEnvironment, null));
        assertEquals("workspaceId is required", e.getMessage());
    }

    @Test
    void noWorkspaceRoles() {
        when(allowWorkspaceRoles.test(authenticationContext)).thenReturn(false);
        PermissionFault e = assertThrows(PermissionFault.class,
                () -> workspaceDocumentSchema.getWorkspace(resolutionEnvironment, workspaceId));
        assertEquals("User does not have access to the workspace", e.getMessage());
    }

    @Test
    void notWorkspaceReviewerOrHigher() {
        when(allowWorkspaceRoles.test(authenticationContext)).thenReturn(true);
        when(allowWorkspaceReviewerOrHigher.test(any(), any(UUID.class))).thenReturn(false);
        PermissionFault e = assertThrows(PermissionFault.class,
                () -> workspaceDocumentSchema.getWorkspace(resolutionEnvironment, workspaceId));
        assertEquals("User does not have permissions to workspace", e.getMessage());
    }

    @Test
    void success() {
        when(allowWorkspaceRoles.test(authenticationContext)).thenReturn(true);
        when(allowWorkspaceReviewerOrHigher.test(any(), any(UUID.class))).thenReturn(true);

        when(workspaceService.fetchById(any(UUID.class))).thenReturn(Mono.just(new Workspace().setId(workspaceId)));

        Workspace workspace = workspaceDocumentSchema.getWorkspace(resolutionEnvironment, workspaceId).join();
        assertNotNull(workspace);
        assertEquals(workspaceId, workspace.getId());
    }

    @Test
    void getCoursewareAncestry() {
        UUID elementId = UUID.randomUUID();
        when(allowWorkspaceReviewerOrHigher.test(authenticationContext, workspaceId)).thenReturn(true);

        when(coursewareService.findCoursewareElementAncestry(elementId))
                .thenReturn(Mono.just(new CoursewareElementAncestry()));

        CoursewareElementAncestry ancestry = workspaceDocumentSchema.getCoursewareAncestry(resolutionEnvironment,
                                                                                           new Workspace()
                .setId(workspaceId), elementId).join();

        assertNotNull(ancestry);

        verify(coursewareService).findCoursewareElementAncestry(elementId);
    }
}
