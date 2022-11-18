package com.smartsparrow.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
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

import com.smartsparrow.iam.data.permission.workspace.AccountWorkspacePermission;
import com.smartsparrow.iam.data.permission.workspace.TeamWorkspacePermission;
import com.smartsparrow.iam.data.permission.workspace.WorkspacePermissionGateway;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.workspace.data.DeletedWorkspace;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.data.WorkspaceAccessGateway;
import com.smartsparrow.workspace.data.WorkspaceAccount;
import com.smartsparrow.workspace.data.WorkspaceAccountCollaborator;
import com.smartsparrow.workspace.data.WorkspaceByTeam;
import com.smartsparrow.workspace.data.WorkspaceGateway;
import com.smartsparrow.workspace.data.WorkspaceTeamCollaborator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

class WorkspaceServiceTest {

    @InjectMocks
    private WorkspaceService workspaceService;
    @Mock
    private WorkspaceGateway workspaceGateway;
    @Mock
    private WorkspacePermissionGateway workspacePermissionGateway;
    @Mock
    private WorkspaceAccessGateway workspaceAccessGateway;
    @Mock
    private TeamService teamService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID accountId2 = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID workspaceId2 = UUID.randomUUID();
    private static final UUID workspaceId3 = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createWorkspace() {
        when(workspaceGateway.persist(any(Workspace.class))).thenReturn(Mono.empty());
        when(workspacePermissionGateway.persist(any(AccountWorkspacePermission.class))).thenReturn(Flux.empty());
        when(workspaceAccessGateway.persist(any(WorkspaceAccount.class))).thenReturn(Flux.empty());
        when(workspaceAccessGateway.persist(any(WorkspaceAccountCollaborator.class))).thenReturn(Flux.empty());

        Workspace result = workspaceService.createWorkspace(subscriptionId, accountId, "name", "description ...").block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("name", result.getName());
        assertEquals("description ...", result.getDescription());

        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceGateway).persist(workspaceCaptor.capture());
        assertEquals(result.getId(), workspaceCaptor.getValue().getId());
        assertEquals("name", workspaceCaptor.getValue().getName());
        assertEquals("description ...", workspaceCaptor.getValue().getDescription());

        ArgumentCaptor<AccountWorkspacePermission> workspacePermissionCaptor = ArgumentCaptor.forClass(AccountWorkspacePermission.class);
        verify(workspacePermissionGateway).persist(workspacePermissionCaptor.capture());
        assertEquals(accountId, workspacePermissionCaptor.getValue().getAccountId());
        assertEquals(result.getId(), workspacePermissionCaptor.getValue().getWorkspaceId());
        assertEquals(PermissionLevel.OWNER, workspacePermissionCaptor.getValue().getPermissionLevel());
    }

    @Test
    void createWorkspace_exception() {
        TestPublisher<Void> error = TestPublisher.create();
        when(workspaceGateway.persist(any(Workspace.class))).thenReturn(error.mono());
        error.error(new RuntimeException("can't create"));

        TestPublisher<Void> publisher1 = TestPublisher.create();
        when(workspacePermissionGateway.persist(any(AccountWorkspacePermission.class))).thenReturn(
                publisher1.flux().doOnComplete(() -> fail("Should not be executed")));
        publisher1.complete();

        StepVerifier.FirstStep<Workspace> verifier = StepVerifier
                .create(workspaceService.createWorkspace(subscriptionId, accountId, "name", "description ..."));

        verifier.verifyError(RuntimeException.class);
    }

    @Test
    void savePermissions() {
        when(workspacePermissionGateway.persist(any(AccountWorkspacePermission.class))).thenReturn(Flux.empty());
        when(workspaceAccessGateway.persist(any(WorkspaceAccount.class))).thenReturn(Flux.empty());
        when(workspaceAccessGateway.persist(any(WorkspaceAccountCollaborator.class))).thenReturn(Flux.empty());

        workspaceService.savePermissions(accountId, workspaceId, PermissionLevel.CONTRIBUTOR);

        ArgumentCaptor<AccountWorkspacePermission> workspacePermissionCaptor = ArgumentCaptor.forClass(AccountWorkspacePermission.class);
        verify(workspacePermissionGateway).persist(workspacePermissionCaptor.capture());
        assertEquals(accountId, workspacePermissionCaptor.getValue().getAccountId());
        assertEquals(workspaceId, workspacePermissionCaptor.getValue().getWorkspaceId());
        assertEquals(PermissionLevel.CONTRIBUTOR, workspacePermissionCaptor.getValue().getPermissionLevel());

        ArgumentCaptor<WorkspaceAccount> workspaceAccountCaptor = ArgumentCaptor.forClass(WorkspaceAccount.class);
        verify(workspaceAccessGateway).persist(workspaceAccountCaptor.capture());
        assertEquals(accountId, workspaceAccountCaptor.getValue().getAccountId());
        assertEquals(workspaceId, workspaceAccountCaptor.getValue().getWorkspaceId());

        ArgumentCaptor<WorkspaceAccountCollaborator> workspaceCollaboratorCaptor = ArgumentCaptor.forClass(WorkspaceAccountCollaborator.class);
        verify(workspaceAccessGateway).persist(workspaceCollaboratorCaptor.capture());
        assertEquals(accountId, workspaceCollaboratorCaptor.getValue().getAccountId());
        assertEquals(workspaceId, workspaceCollaboratorCaptor.getValue().getWorkspaceId());
        assertEquals(PermissionLevel.CONTRIBUTOR, workspaceCollaboratorCaptor.getValue().getPermissionLevel());

    }

    @Test
    void deletePermissions() {
        when(workspacePermissionGateway.delete(any(AccountWorkspacePermission.class))).thenReturn(Flux.empty());
        when(workspaceAccessGateway.delete(any(WorkspaceAccount.class))).thenReturn(Flux.empty());
        when(workspaceAccessGateway.delete(any(WorkspaceAccountCollaborator.class))).thenReturn(Flux.empty());

        workspaceService.deletePermissions(accountId, workspaceId);

        ArgumentCaptor<AccountWorkspacePermission> workspacePermissionCaptor = ArgumentCaptor.forClass(AccountWorkspacePermission.class);
        verify(workspacePermissionGateway).delete(workspacePermissionCaptor.capture());
        assertEquals(accountId, workspacePermissionCaptor.getValue().getAccountId());
        assertEquals(workspaceId, workspacePermissionCaptor.getValue().getWorkspaceId());
        assertNull(workspacePermissionCaptor.getValue().getPermissionLevel());

        ArgumentCaptor<WorkspaceAccount> workspaceAccountCaptor = ArgumentCaptor.forClass(WorkspaceAccount.class);
        verify(workspaceAccessGateway).delete(workspaceAccountCaptor.capture());
        assertEquals(accountId, workspaceAccountCaptor.getValue().getAccountId());
        assertEquals(workspaceId, workspaceAccountCaptor.getValue().getWorkspaceId());

        ArgumentCaptor<WorkspaceAccountCollaborator> workspaceCollaboratorCaptor = ArgumentCaptor.forClass(WorkspaceAccountCollaborator.class);
        verify(workspaceAccessGateway).delete(workspaceCollaboratorCaptor.capture());
        assertEquals(accountId, workspaceCollaboratorCaptor.getValue().getAccountId());
        assertEquals(workspaceId, workspaceCollaboratorCaptor.getValue().getWorkspaceId());
        assertNull(workspaceCollaboratorCaptor.getValue().getPermissionLevel());
    }

    @Test
    void updateWorkspace() {
        Workspace workspace = new Workspace().setId(workspaceId).setName("updatedName").setDescription("updatedDescription");
        when(workspaceGateway.findById(eq(workspaceId))).thenReturn(Mono.just(new Workspace().setId(workspaceId)));
        when(workspaceGateway.persist(eq(workspace))).thenReturn(Mono.empty());

        StepVerifier.FirstStep<Workspace> verifier =
                StepVerifier.create(workspaceService.updateWorkspace(workspaceId, "updatedName", "updatedDescription"));

        verifier.expectNext(workspace).verifyComplete();
        verify(workspaceGateway).persist(eq(workspace));
        verify(workspaceGateway).findById(eq(workspaceId));
    }

    @Test
    void deleteWorkspace() {
        Workspace workspace = new Workspace().setId(workspaceId).setName(null).setDescription(null).setSubscriptionId(subscriptionId);
        WorkspaceAccount workspaceAccount = new WorkspaceAccount().setAccountId(accountId).setWorkspaceId(workspaceId);
        WorkspaceAccountCollaborator workspaceAccountCollaborator = new WorkspaceAccountCollaborator().setAccountId(accountId).setWorkspaceId(workspaceId);
        WorkspaceTeamCollaborator teamCollaborator = new WorkspaceTeamCollaborator().setTeamId(teamId).setWorkspaceId(workspaceId);
        when(workspaceGateway.delete(workspace)).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.findAccountsByWorkspace(workspaceId)).thenReturn(Flux.just(workspaceAccountCollaborator));
        when(workspaceAccessGateway.delete(workspaceAccount)).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.findTeamsByWorkspace(workspaceId)).thenReturn(Flux.just(teamCollaborator));
        when(workspaceAccessGateway.delete(any(WorkspaceByTeam.class))).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.delete(teamCollaborator)).thenReturn(Flux.just(new Void[]{}));
        when(workspaceGateway.persist(any(DeletedWorkspace.class))).thenReturn(Flux.just(new Void[]{}));

        workspaceService.deleteWorkspace(workspaceId, "name", accountId, subscriptionId);

        verify(workspaceGateway).delete(eq(workspace));
        verify(workspaceAccessGateway).findAccountsByWorkspace(eq(workspaceId));
        verify(workspaceAccessGateway).findTeamsByWorkspace(eq(workspaceId));
        verify(workspaceGateway).persist(any(DeletedWorkspace.class));
    }

    @Test
    void fetchWorkspaces() {
        Workspace workspace1 = new Workspace().setId(workspaceId).setName("Name1");
        Workspace workspace2 = new Workspace().setId(workspaceId2).setName("Name2");
        when(workspaceAccessGateway.findWorkspacesByAccount(eq(accountId))).thenReturn(Flux.just(workspaceId, workspaceId2));
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(workspaceGateway.findById(eq(workspaceId))).thenReturn(Mono.just(workspace1));
        when(workspaceGateway.findById(eq(workspaceId2))).thenReturn(Mono.just(workspace2));

        //it preserves order in a test only because emitting items by workspaceGateway.findById happens fast, in real world the order will not be saved
        StepVerifier.create(workspaceService.fetchWorkspaces(accountId)).expectNext(workspace1, workspace2).verifyComplete();
    }

    @Test
    void fetchWorkspaces_teamsAndAccounts() {
        UUID teamId = UUID.randomUUID();
        Workspace workspace1 = new Workspace().setId(workspaceId).setName("Name1");
        Workspace workspace2 = new Workspace().setId(workspaceId2).setName("Name2");
        Workspace workspace3 = new Workspace().setId(workspaceId3).setName("Name3");
        when(workspaceAccessGateway.findWorkspacesByAccount(eq(accountId))).thenReturn(Flux.just(workspaceId, workspaceId2));
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findWorkspacesByTeam(teamId)).thenReturn(Flux.just(workspaceId3, workspaceId2));
        when(workspaceGateway.findById(eq(workspaceId))).thenReturn(Mono.just(workspace1));
        when(workspaceGateway.findById(eq(workspaceId2))).thenReturn(Mono.just(workspace2));
        when(workspaceGateway.findById(eq(workspaceId3))).thenReturn(Mono.just(workspace3));

        //it preserves order in a test only because emitting items by workspaceGateway.findById happens fast, in real world the order will not be saved
        StepVerifier.create(workspaceService.fetchWorkspaces(accountId)).expectNext(workspace1, workspace2, workspace3).verifyComplete();
    }

    @Test
    void fetchWorkspaces_onlyTeams() {
        UUID teamId = UUID.randomUUID();
        Workspace workspace1 = new Workspace().setId(workspaceId).setName("Name1");
        when(workspaceAccessGateway.findWorkspacesByAccount(eq(accountId))).thenReturn(Flux.empty());
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findWorkspacesByTeam(teamId)).thenReturn(Flux.just(workspaceId));
        when(workspaceGateway.findById(eq(workspaceId))).thenReturn(Mono.just(workspace1));

        //it preserves order in a test only because emitting items by workspaceGateway.findById happens fast, in real world the order will not be saved
        StepVerifier.create(workspaceService.fetchWorkspaces(accountId)).expectNext(workspace1).verifyComplete();
    }

    @Test
    void fetchWorkspaces_noWorkspaces() {
        UUID teamId = UUID.randomUUID();
        when(workspaceAccessGateway.findWorkspacesByAccount(eq(accountId))).thenReturn(Flux.empty());
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findWorkspacesByTeam(teamId)).thenReturn(Flux.empty());

        StepVerifier.create(workspaceService.fetchWorkspaces(accountId)).verifyComplete();
    }

    @Test
    void fetchById_noWorkspace() {
        when(workspaceGateway.findById(eq(workspaceId))).thenReturn(Mono.empty());

        StepVerifier.create(workspaceService.fetchById(workspaceId)).verifyComplete();
    }

    @Test
    void fetchCollaborators() {
        WorkspaceAccountCollaborator workspaceAccountCollaborator1 = new WorkspaceAccountCollaborator().setWorkspaceId(workspaceId).setAccountId(accountId);
        WorkspaceAccountCollaborator workspaceAccountCollaborator2 = new WorkspaceAccountCollaborator().setWorkspaceId(workspaceId).setAccountId(accountId2);
        when(workspaceAccessGateway.findAccountsByWorkspace(workspaceId)).thenReturn(Flux.just(workspaceAccountCollaborator1, workspaceAccountCollaborator2));

        StepVerifier.create(workspaceService.fetchAccountCollaborators(workspaceId))
                .expectNext(workspaceAccountCollaborator1, workspaceAccountCollaborator2)
                .verifyComplete();
    }

    @Test
    void fetchCollaborators_noCollaborators() {
        when(workspaceAccessGateway.findAccountsByWorkspace(workspaceId)).thenReturn(Flux.empty());

        StepVerifier.create(workspaceService.fetchAccountCollaborators(workspaceId)).verifyComplete();
    }

    @Test
    void fetchPermission() {
        AccountWorkspacePermission permission = new AccountWorkspacePermission()
                .setAccountId(accountId).setWorkspaceId(workspaceId).setPermissionLevel(PermissionLevel.CONTRIBUTOR);
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.just(permission));

        StepVerifier.create(workspaceService.fetchPermission(accountId, workspaceId)).expectNext(permission).verifyComplete();
    }

    @Test
    void fetchPermission_noPermission() {
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.empty());

        StepVerifier.create(workspaceService.fetchPermission(accountId, workspaceId)).verifyComplete();
    }

    @Test
    void saveTeamPermission_nullTeamId() {
        NullPointerException e = assertThrows(NullPointerException.class, ()-> workspaceService
                .saveTeamPermission(null, workspaceId, PermissionLevel.REVIEWER).blockLast());

        assertEquals("teamId is required", e.getMessage());
    }

    @Test
    void saveTeamPermission_nullWorkspaceId() {
        UUID teamId = UUID.randomUUID();
        NullPointerException e = assertThrows(NullPointerException.class, ()-> workspaceService
                .saveTeamPermission(teamId, null, PermissionLevel.REVIEWER).blockLast());

        assertEquals("workspaceId is required", e.getMessage());
    }

    @Test
    void saveTeamPermission_nullPermissionLevel() {
        UUID teamId = UUID.randomUUID();
        NullPointerException e = assertThrows(NullPointerException.class, ()-> workspaceService
                .saveTeamPermission(teamId, workspaceId, null).blockLast());

        assertEquals("permissionLevel is required", e.getMessage());
    }

    @Test
    void saveTeamPermission_success() {
        UUID teamId = UUID.randomUUID();

        when(workspacePermissionGateway.persist(any(TeamWorkspacePermission.class))).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.persist(any(WorkspaceByTeam.class))).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.persist(any(WorkspaceTeamCollaborator.class))).thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<TeamWorkspacePermission> teamWorkspacePermission = ArgumentCaptor.forClass(TeamWorkspacePermission.class);
        ArgumentCaptor<WorkspaceByTeam> workspaceByTeam = ArgumentCaptor.forClass(WorkspaceByTeam.class);
        ArgumentCaptor<WorkspaceTeamCollaborator> workspaceTeamCollaborator = ArgumentCaptor.forClass(WorkspaceTeamCollaborator.class);

        workspaceService.saveTeamPermission(teamId, workspaceId, PermissionLevel.REVIEWER).blockLast();

        verify(workspacePermissionGateway).persist(teamWorkspacePermission.capture());
        verify(workspaceAccessGateway).persist(workspaceByTeam.capture());
        verify(workspaceAccessGateway).persist(workspaceTeamCollaborator.capture());


        assertEquals(teamId, teamWorkspacePermission.getValue().getTeamId());
        assertEquals(workspaceId, teamWorkspacePermission.getValue().getWorkspaceId());
        assertEquals(PermissionLevel.REVIEWER, teamWorkspacePermission.getValue().getPermissionLevel());

        assertEquals(teamId, workspaceTeamCollaborator.getValue().getTeamId());
        assertEquals(workspaceId, workspaceTeamCollaborator.getValue().getWorkspaceId());
        assertEquals(PermissionLevel.REVIEWER, workspaceTeamCollaborator.getValue().getPermissionLevel());

        assertEquals(teamId, workspaceByTeam.getValue().getTeamId());
        assertEquals(workspaceId, workspaceByTeam.getValue().getWorkspaceId());

    }

    @Test
    void deleteTeamPermission_nullTeamId() {
        NullPointerException e = assertThrows(NullPointerException.class, ()-> workspaceService
                .deleteTeamPermission(null, workspaceId).blockLast());

        assertEquals("teamId is required", e.getMessage());    }

    @Test
    void deleteTeamPermission_nullWorkspaceId() {
        UUID teamId = UUID.randomUUID();
        NullPointerException e = assertThrows(NullPointerException.class, ()-> workspaceService
                .deleteTeamPermission(teamId, null).blockLast());

        assertEquals("workspaceId is required", e.getMessage());
    }

    @Test
    void deleteTeamPermission_success() {
        UUID teamId = UUID.randomUUID();

        when(workspacePermissionGateway.delete(any(TeamWorkspacePermission.class))).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.delete(any(WorkspaceTeamCollaborator.class))).thenReturn(Flux.just(new Void[]{}));
        when(workspaceAccessGateway.delete(any(WorkspaceByTeam.class))).thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<TeamWorkspacePermission> teamWorkspacePermission = ArgumentCaptor.forClass(TeamWorkspacePermission.class);
        ArgumentCaptor<WorkspaceByTeam> workspaceByTeam = ArgumentCaptor.forClass(WorkspaceByTeam.class);
        ArgumentCaptor<WorkspaceTeamCollaborator> workspaceTeamCollaborator = ArgumentCaptor.forClass(WorkspaceTeamCollaborator.class);

        workspaceService.deleteTeamPermission(teamId, workspaceId).blockLast();

        verify(workspacePermissionGateway).delete(teamWorkspacePermission.capture());
        verify(workspaceAccessGateway).delete(workspaceByTeam.capture());
        verify(workspaceAccessGateway).delete(workspaceTeamCollaborator.capture());

        assertEquals(teamId, teamWorkspacePermission.getValue().getTeamId());
        assertEquals(workspaceId, teamWorkspacePermission.getValue().getWorkspaceId());

        assertEquals(teamId, workspaceTeamCollaborator.getValue().getTeamId());
        assertEquals(workspaceId, workspaceTeamCollaborator.getValue().getWorkspaceId());

        assertEquals(teamId, workspaceByTeam.getValue().getTeamId());
        assertEquals(workspaceId, workspaceByTeam.getValue().getWorkspaceId());

    }

    @Test
    void findHighestPermissionLevel_errorFetchingTeams() {

        TestPublisher<TeamAccount> teamAccountTestPublisher = TestPublisher.create();
        teamAccountTestPublisher.error(new RuntimeException(":face_palm:"));

        when(teamService.findTeamsForAccount(accountId)).thenReturn(teamAccountTestPublisher.flux());
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.empty());

        assertThrows(Exception.class, () -> workspaceService.findHighestPermissionLevel(accountId, workspaceId).block());
    }

    @Test
    void findHighestPermissionLevel_noTeams() {
        AccountWorkspacePermission accountPermission = new AccountWorkspacePermission()
                .setAccountId(accountId)
                .setWorkspaceId(workspaceId)
                .setPermissionLevel(PermissionLevel.REVIEWER);
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.just(accountPermission));

        PermissionLevel permission = workspaceService.findHighestPermissionLevel(accountId, workspaceId).block();

        assertEquals(PermissionLevel.REVIEWER, permission);
    }

    @Test
    void findHighestPermissionLevel_noTeamPermission() {
        UUID teamId = UUID.randomUUID();
        AccountWorkspacePermission accountPermission = new AccountWorkspacePermission()
                .setAccountId(accountId)
                .setWorkspaceId(workspaceId)
                .setPermissionLevel(PermissionLevel.REVIEWER);

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findTeamPermission(teamId, workspaceId)).thenReturn(Mono.empty());
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.just(accountPermission));

        PermissionLevel permission = workspaceService.findHighestPermissionLevel(accountId, workspaceId).block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.REVIEWER, permission);
    }

    @Test
    void findHighestPermissionLevel_noAccountPermission() {
        UUID teamId = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findTeamPermission(teamId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.empty());

        PermissionLevel permission = workspaceService.findHighestPermissionLevel(accountId, workspaceId).block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.CONTRIBUTOR, permission);
    }

    @Test
    void findHighestPermissionLevel_teamHighest() {
        UUID teamId = UUID.randomUUID();
        AccountWorkspacePermission accountPermission = new AccountWorkspacePermission()
                .setAccountId(accountId)
                .setWorkspaceId(workspaceId)
                .setPermissionLevel(PermissionLevel.REVIEWER);

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findTeamPermission(teamId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.just(accountPermission));

        PermissionLevel permission = workspaceService.findHighestPermissionLevel(accountId, workspaceId).block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.CONTRIBUTOR, permission);
    }

    @Test
    void findHighestPermissionLevel_accountHighest() {
        UUID teamId = UUID.randomUUID();
        AccountWorkspacePermission accountPermission = new AccountWorkspacePermission()
                .setAccountId(accountId)
                .setWorkspaceId(workspaceId)
                .setPermissionLevel(PermissionLevel.OWNER);

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(workspaceAccessGateway.findTeamPermission(teamId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(workspacePermissionGateway.findPermission(accountId, workspaceId)).thenReturn(Mono.just(accountPermission));

        PermissionLevel permission = workspaceService.findHighestPermissionLevel(accountId, workspaceId).block();

        assertNotNull(permission);
        assertEquals(PermissionLevel.OWNER, permission);
    }
}
