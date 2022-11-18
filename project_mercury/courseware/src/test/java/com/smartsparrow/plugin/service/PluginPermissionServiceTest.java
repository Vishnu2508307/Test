package com.smartsparrow.plugin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.plugin.AccountPluginPermission;
import com.smartsparrow.iam.data.permission.plugin.PluginPermissionGateway;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginAccessGateway;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.data.PluginTeamCollaborator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class PluginPermissionServiceTest {

    @InjectMocks
    private PluginPermissionService pluginPermissionService;

    @Mock
    private PluginAccessGateway pluginAccessGateway;

    @Mock
    private PluginPermissionGateway pluginPermissionGateway;

    @Mock
    private TeamService teamService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findHighestPermissionLevel_teamPermissionHigherThanAccountPermission() {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamIdOne),
                new TeamAccount().setTeamId(teamIdTwo)
        ));

        when(pluginPermissionGateway.fetchTeamPermission(teamIdOne, pluginId)).thenReturn(Mono.empty());
        when(pluginPermissionGateway.fetchTeamPermission(teamIdTwo, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(pluginPermissionGateway.fetchAccountPermission(accountId, pluginId))
                .thenReturn(Flux.just(new AccountPluginPermission().setPermissionLevel(PermissionLevel.REVIEWER)));

        PermissionLevel result = pluginPermissionService.findHighestPermissionLevel(accountId, pluginId).block();

        assertEquals(PermissionLevel.CONTRIBUTOR, result);
    }

    @Test
    void findHighestPermissionLevel_accountPermissionHigherThanTeamPermission() {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamIdOne),
                new TeamAccount().setTeamId(teamIdTwo)
        ));

        when(pluginPermissionGateway.fetchTeamPermission(teamIdOne, pluginId)).thenReturn(Mono.empty());
        when(pluginPermissionGateway.fetchTeamPermission(teamIdTwo, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(pluginPermissionGateway.fetchAccountPermission(accountId, pluginId))
                .thenReturn(Flux.just(new AccountPluginPermission().setPermissionLevel(PermissionLevel.OWNER)));

        PermissionLevel result = pluginPermissionService.findHighestPermissionLevel(accountId, pluginId).block();

        assertEquals(PermissionLevel.OWNER, result);
    }

    @Test
    void findHighestPermissionLevel_teamPermissionNotFound() {
        UUID teamId = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamId)
        ));

        when(pluginPermissionGateway.fetchTeamPermission(teamId, pluginId)).thenReturn(Mono.empty());

        when(pluginPermissionGateway.fetchAccountPermission(accountId, pluginId))
                .thenReturn(Flux.just(new AccountPluginPermission().setPermissionLevel(PermissionLevel.REVIEWER)));

        PermissionLevel result = pluginPermissionService.findHighestPermissionLevel(accountId, pluginId).block();

        assertEquals(PermissionLevel.REVIEWER, result);
    }

    @Test
    void findHighestPermissionLevel_accountPermissionNotFound() {
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamIdOne),
                new TeamAccount().setTeamId(teamIdTwo)
        ));

        when(pluginPermissionGateway.fetchTeamPermission(teamIdOne, pluginId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        when(pluginPermissionGateway.fetchTeamPermission(teamIdTwo, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(pluginPermissionGateway.fetchAccountPermission(accountId, pluginId))
                .thenReturn(Flux.empty());

        PermissionLevel result = pluginPermissionService.findHighestPermissionLevel(accountId, pluginId).block();

        assertEquals(PermissionLevel.OWNER, result);
    }

    @Test
    void findHighestPermissionLevel_noneFound() {
        UUID teamId = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamId)
        ));

        when(pluginPermissionGateway.fetchTeamPermission(teamId, pluginId)).thenReturn(Mono.empty());
        when(pluginPermissionGateway.fetchAccountPermission(accountId, pluginId))
                .thenReturn(Flux.empty());

        PermissionLevel result = pluginPermissionService.findHighestPermissionLevel(accountId, pluginId).block();

        assertNull(result);

    }

    @Test
    void findHighestPermissionLevel_errorOnTeamService() {
        TestPublisher<TeamAccount> teamAccountTestPublisher = TestPublisher.create();
        teamAccountTestPublisher.error(new RuntimeException("unexpected error occurred"));

        when(teamService.findTeamsForAccount(accountId)).thenReturn(teamAccountTestPublisher.flux());
        when(pluginPermissionGateway.fetchAccountPermission(accountId, pluginId))
                .thenReturn(Flux.empty());

        PermissionLevel result = pluginPermissionService.findHighestPermissionLevel(accountId, pluginId).block();

        assertNull(result);
    }

    @Test
    void deleteAccountPermissions() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        PluginAccountCollaborator coll1 = new PluginAccountCollaborator().setAccountId(accountId1);
        PluginAccountCollaborator coll2 = new PluginAccountCollaborator().setAccountId(accountId2);
        when(pluginAccessGateway.fetchAccounts(pluginId)).thenReturn(Flux.just(coll1, coll2));

        PluginPermissionService spy = spy(pluginPermissionService);
        doReturn(Flux.empty()).when(spy).deleteAccountPermission(any(), eq(pluginId));

        spy.deleteAccountPermissions(pluginId).subscribe();

        verify(spy, times(1)).deleteAccountPermission(accountId1, pluginId);
        verify(spy, times(1)).deleteAccountPermission(accountId2, pluginId);
    }

    @Test
    void deleteAccountPermissions_noAccounts() {
        when(pluginAccessGateway.fetchAccounts(pluginId)).thenReturn(Flux.empty());
        PluginPermissionService spy = spy(pluginPermissionService);
        doReturn(Flux.empty()).when(spy).deleteAccountPermission(any(), eq(pluginId));

        spy.deleteAccountPermissions(pluginId).subscribe();

        verify(spy, never()).deleteAccountPermission(any(), eq(pluginId));
    }

    @Test
    void deleteTeamPermissions() {
        UUID teamId1 = UUID.randomUUID();
        UUID teamId2 = UUID.randomUUID();
        PluginTeamCollaborator coll1 = new PluginTeamCollaborator().setTeamId(teamId1);
        PluginTeamCollaborator coll2 = new PluginTeamCollaborator().setTeamId(teamId2);
        when(pluginAccessGateway.fetchTeams(pluginId)).thenReturn(Flux.just(coll1, coll2));

        PluginPermissionService spy = spy(pluginPermissionService);
        doReturn(Flux.empty()).when(spy).deleteTeamPermission(any(), eq(pluginId));

        spy.deleteTeamPermissions(pluginId).subscribe();

        verify(spy, times(1)).deleteTeamPermission(teamId1, pluginId);
        verify(spy, times(1)).deleteTeamPermission(teamId2, pluginId);
    }

    @Test
    void deleteTeamPermissions_noTeams() {
        when(pluginAccessGateway.fetchTeams(pluginId)).thenReturn(Flux.empty());
        PluginPermissionService spy = spy(pluginPermissionService);
        doReturn(Flux.empty()).when(spy).deleteTeamPermission(any(), eq(pluginId));

        spy.deleteTeamPermissions(pluginId).subscribe();

        verify(spy, never()).deleteTeamPermission(any(), eq(pluginId));
    }
}
