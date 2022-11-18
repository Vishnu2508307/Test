package com.smartsparrow.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.workspace.AccountProjectPermission;
import com.smartsparrow.iam.data.permission.workspace.ProjectPermissionGateway;
import com.smartsparrow.iam.data.permission.workspace.TeamProjectPermission;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.workspace.data.ProjectAccessGateway;
import com.smartsparrow.workspace.data.ProjectAccount;
import com.smartsparrow.workspace.data.ProjectAccountCollaborator;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.ProjectTeam;
import com.smartsparrow.workspace.data.ProjectTeamCollaborator;
import com.smartsparrow.workspace.data.WorkspaceProject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ProjectPermissionServiceTest {

    @InjectMocks
    private ProjectPermissionService projectPermissionService;

    @Mock
    private ProjectAccessGateway projectAccessGateway;

    @Mock
    private ProjectPermissionGateway projectPermissionGateway;

    @Mock
    private ProjectGateway projectGateway;

    @Mock
    private TeamService teamService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(projectGateway.findWorkspaceForProject(projectId)).thenReturn(Mono.just(new WorkspaceProject()
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)));
    }

    @Test
    void saveAccountPermission() {
        when(projectAccessGateway.persist(any(ProjectAccountCollaborator.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(projectPermissionGateway.persist(any(AccountProjectPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<ProjectAccountCollaborator> collaboratorCaptor = ArgumentCaptor.forClass(ProjectAccountCollaborator.class);
        ArgumentCaptor<AccountProjectPermission> permissionCaptor = ArgumentCaptor.forClass(AccountProjectPermission.class);

        projectPermissionService.saveAccountPermission(accountId, projectId, PermissionLevel.CONTRIBUTOR)
                .blockFirst();

        verify(projectAccessGateway).persist(collaboratorCaptor.capture(), eq(workspaceId));
        verify(projectPermissionGateway).persist(permissionCaptor.capture());

        ProjectAccountCollaborator collaborator = collaboratorCaptor.getValue();
        AccountProjectPermission permission = permissionCaptor.getValue();

        assertNotNull(collaborator);
        assertEquals(accountId, collaborator.getAccountId());
        assertEquals(projectId, collaborator.getProjectId());
        assertEquals(PermissionLevel.CONTRIBUTOR, collaborator.getPermissionLevel());

        assertNotNull(permission);
        assertEquals(accountId, permission.getAccountId());
        assertEquals(projectId, permission.getProjectId());
        assertEquals(PermissionLevel.CONTRIBUTOR, permission.getPermissionLevel());
    }

    @Test
    void saveTeamPermission() {
        when(projectAccessGateway.persist(any(ProjectTeamCollaborator.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(projectPermissionGateway.persist(any(TeamProjectPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<ProjectTeamCollaborator> collaboratorCaptor = ArgumentCaptor.forClass(ProjectTeamCollaborator.class);
        ArgumentCaptor<TeamProjectPermission> permissionCaptor = ArgumentCaptor.forClass(TeamProjectPermission.class);

        projectPermissionService.saveTeamPermission(teamId, projectId, PermissionLevel.CONTRIBUTOR)
                .blockFirst();

        verify(projectAccessGateway).persist(collaboratorCaptor.capture(), eq(workspaceId));
        verify(projectPermissionGateway).persist(permissionCaptor.capture());

        ProjectTeamCollaborator collaborator = collaboratorCaptor.getValue();
        TeamProjectPermission permission = permissionCaptor.getValue();

        assertNotNull(collaborator);
        assertEquals(teamId, collaborator.getTeamId());
        assertEquals(projectId, collaborator.getProjectId());
        assertEquals(PermissionLevel.CONTRIBUTOR, collaborator.getPermissionLevel());

        assertNotNull(permission);
        assertEquals(teamId, permission.getTeamId());
        assertEquals(projectId, permission.getProjectId());
        assertEquals(PermissionLevel.CONTRIBUTOR, permission.getPermissionLevel());
    }

    @Test
    void deleteAccountPermission() {
        when(projectAccessGateway.delete(any(ProjectAccount.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(projectPermissionGateway.delete(any(AccountProjectPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<ProjectAccount> projectAccountCaptor = ArgumentCaptor.forClass(ProjectAccount.class);
        ArgumentCaptor<AccountProjectPermission> permissionCaptor = ArgumentCaptor.forClass(AccountProjectPermission.class);

        projectPermissionService.deleteAccountPermission(accountId, projectId)
                .blockFirst();

        verify(projectAccessGateway).delete(projectAccountCaptor.capture());
        verify(projectPermissionGateway).delete(permissionCaptor.capture());

        ProjectAccount projectAccount = projectAccountCaptor.getValue();
        AccountProjectPermission permission = permissionCaptor.getValue();

        assertNotNull(projectAccount);
        assertEquals(accountId, projectAccount.getAccountId());
        assertEquals(projectId, projectAccount.getProjectId());

        assertNotNull(permission);
        assertEquals(accountId, permission.getAccountId());
        assertEquals(projectId, permission.getProjectId());
        // this method does not require the following param to be defined
        assertNull(permission.getPermissionLevel());
    }

    @Test
    void deleteTeamPermission() {
        when(projectAccessGateway.delete(any(ProjectTeam.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(projectPermissionGateway.delete(any(TeamProjectPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<ProjectTeam> projectAccountCaptor = ArgumentCaptor.forClass(ProjectTeam.class);
        ArgumentCaptor<TeamProjectPermission> permissionCaptor = ArgumentCaptor.forClass(TeamProjectPermission.class);

        projectPermissionService.deleteTeamPermission(teamId, projectId)
                .blockFirst();

        verify(projectAccessGateway).delete(projectAccountCaptor.capture());
        verify(projectPermissionGateway).delete(permissionCaptor.capture());

        ProjectTeam projectTeam = projectAccountCaptor.getValue();
        TeamProjectPermission permission = permissionCaptor.getValue();

        assertNotNull(projectTeam);
        assertEquals(teamId, projectTeam.getTeamId());
        assertEquals(projectId, projectTeam.getProjectId());

        assertNotNull(permission);
        assertEquals(teamId, permission.getTeamId());
        assertEquals(projectId, permission.getProjectId());
        // this method does not require the following param to be defined
        assertNull(permission.getPermissionLevel());
    }

    @Test
    void fetchAccountPermission() {
        when(projectPermissionGateway.findAccountPermission(accountId, projectId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        PermissionLevel permission = projectPermissionService.fetchAccountPermission(accountId, projectId)
                .block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.CONTRIBUTOR, permission);

        verify(projectPermissionGateway).findAccountPermission(accountId, projectId);
    }

    @Test
    void fetchTeamPermission() {
        when(projectPermissionGateway.findTeamPermission(teamId, projectId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        PermissionLevel permission = projectPermissionService.fetchTeamPermission(teamId, projectId)
                .block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.CONTRIBUTOR, permission);

        verify(projectPermissionGateway).findTeamPermission(teamId, projectId);
    }

    @Test
    void findHighestPermissionLevel() {
        when(teamService.findTeamsForAccount(accountId))
                .thenReturn(Flux.just(new TeamAccount()
                        .setAccountId(accountId)
                        .setTeamId(teamId)));
        when(projectPermissionGateway.findAccountPermission(accountId, projectId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        when(projectPermissionGateway.findTeamPermission(teamId, projectId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        PermissionLevel permission = projectPermissionService.findHighestPermissionLevel(accountId, projectId)
                .block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.CONTRIBUTOR, permission);
    }
}
