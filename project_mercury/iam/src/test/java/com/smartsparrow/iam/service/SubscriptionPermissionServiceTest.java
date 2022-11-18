package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.subscription.AccountSubscriptionPermission;
import com.smartsparrow.iam.data.permission.subscription.SubscriptionPermissionGateway;
import com.smartsparrow.iam.data.permission.subscription.TeamSubscriptionPermission;
import com.smartsparrow.iam.data.team.TeamAccount;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class SubscriptionPermissionServiceTest {

    @InjectMocks
    private SubscriptionPermissionService subscriptionPermissionService;

    @Mock
    private TeamService teamService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @Mock
    private SubscriptionPermissionGateway subscriptionPermissionGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void saveAccountPermission_noAccountId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.saveAccountPermission(null, subscriptionId, PermissionLevel.CONTRIBUTOR)
                        .blockLast());

        assertEquals("accountId is required", e.getMessage());
    }

    @Test
    void saveAccountPermission_noSubscriptionId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.saveAccountPermission(accountId, null, PermissionLevel.CONTRIBUTOR)
                        .blockLast());

        assertEquals("subscriptionId is required", e.getMessage());
    }

    @Test
    void saveAccountPermission_noPermissionLevel() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.saveAccountPermission(accountId, subscriptionId, null)
                        .blockLast());

        assertEquals("permissionLevel is required", e.getMessage());
    }

    @Test
    void saveAccountPermission_success() {

        when(subscriptionPermissionGateway.saveAccountPermission(any(AccountSubscriptionPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<AccountSubscriptionPermission> captor = ArgumentCaptor.forClass(AccountSubscriptionPermission.class);

        subscriptionPermissionService.saveAccountPermission(accountId, subscriptionId, PermissionLevel.CONTRIBUTOR)
                .blockLast();

        verify(subscriptionPermissionGateway).saveAccountPermission(captor.capture());

        AccountSubscriptionPermission permission = captor.getValue();

        assertEquals(accountId, permission.getAccountId());
        assertEquals(subscriptionId, permission.getSubscriptionId());
        assertEquals(PermissionLevel.CONTRIBUTOR, permission.getPermissionLevel());
    }

    @Test
    void deleteAccountPermission_noAccountId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.deleteAccountPermission(null, subscriptionId)
                        .blockLast());

        assertEquals("accountId is required", e.getMessage());
    }

    @Test
    void deleteAccountPermission_noSubscriptionId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.deleteAccountPermission(accountId, null)
                        .blockLast());

        assertEquals("subscriptionId is required", e.getMessage());
    }

    @Test
    void deleteAccountPermission_success() {
        when(subscriptionPermissionGateway.deleteAccountPermission(any(AccountSubscriptionPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<AccountSubscriptionPermission> captor = ArgumentCaptor.forClass(AccountSubscriptionPermission.class);

        subscriptionPermissionService.deleteAccountPermission(accountId, subscriptionId)
                .blockLast();

        verify(subscriptionPermissionGateway).deleteAccountPermission(captor.capture());

        AccountSubscriptionPermission permission = captor.getValue();

        assertEquals(accountId, permission.getAccountId());
        assertEquals(subscriptionId, permission.getSubscriptionId());
        assertNull(permission.getPermissionLevel());
    }

    @Test
    void saveTeamPermission_noTeamId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.saveTeamPermission(null, subscriptionId, PermissionLevel.CONTRIBUTOR)
                        .blockLast());

        assertEquals("teamId is required", e.getMessage());
    }

    @Test
    void saveTeamPermission_noSubscriptionId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.saveTeamPermission(teamId, null, PermissionLevel.CONTRIBUTOR)
                        .blockLast());

        assertEquals("subscriptionId is required", e.getMessage());
    }

    @Test
    void saveTeamPermission_noPermissionLevel() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.saveTeamPermission(teamId, subscriptionId, null)
                        .blockLast());

        assertEquals("permissionLevel is required", e.getMessage());
    }

    @Test
    void saveTeamPermission_success() {
        when(subscriptionPermissionGateway.saveTeamPermission(any(TeamSubscriptionPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<TeamSubscriptionPermission> captor = ArgumentCaptor.forClass(TeamSubscriptionPermission.class);

        subscriptionPermissionService.saveTeamPermission(teamId, subscriptionId, PermissionLevel.CONTRIBUTOR)
                .blockLast();

        verify(subscriptionPermissionGateway).saveTeamPermission(captor.capture());

        TeamSubscriptionPermission permission = captor.getValue();

        assertEquals(teamId, permission.getTeamId());
        assertEquals(subscriptionId, permission.getSubscriptionId());
        assertEquals(PermissionLevel.CONTRIBUTOR, permission.getPermissionLevel());
    }

    @Test
    void deleteTeamPermission_noTeamId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.deleteTeamPermission(null, subscriptionId)
                        .blockLast());

        assertEquals("teamId is required", e.getMessage());
    }

    @Test
    void deleteTeamPermission_noSubscriptionId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> subscriptionPermissionService.deleteTeamPermission(teamId, null)
                        .blockLast());

        assertEquals("subscriptionId is required", e.getMessage());
    }

    @Test
    void deleteTeamPermission_success() {
        when(subscriptionPermissionGateway.deleteTeamPermission(any(TeamSubscriptionPermission.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<TeamSubscriptionPermission> captor = ArgumentCaptor.forClass(TeamSubscriptionPermission.class);

        subscriptionPermissionService.deleteTeamPermission(teamId, subscriptionId)
                .blockLast();

        verify(subscriptionPermissionGateway).deleteTeamPermission(captor.capture());

        TeamSubscriptionPermission permission = captor.getValue();

        assertEquals(teamId, permission.getTeamId());
        assertEquals(subscriptionId, permission.getSubscriptionId());
        assertNull(permission.getPermissionLevel());
    }

    @Test
    void findHighestPermissionLevel_noTeamsNoPermission() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        when(subscriptionPermissionGateway.fetchAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.empty());

        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId).block();

        assertNull(permissionLevel);
    }

    @Test
    void findHighestPermissionLevel_noTeams() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        when(subscriptionPermissionGateway.fetchAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setAccountId(accountId)
                        .setSubscriptionId(subscriptionId)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId).block();

        assertNotNull(permissionLevel);
        assertEquals(PermissionLevel.CONTRIBUTOR, permissionLevel);
    }

    @Test
    void findHighestPermissionLevel_noAccountPermission() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));

        when(subscriptionPermissionGateway.fetchAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.empty());

        when(subscriptionPermissionGateway.fetchTeamPermission(teamId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId).block();

        assertNotNull(permissionLevel);
        assertEquals(PermissionLevel.CONTRIBUTOR, permissionLevel);
    }

    @Test
    void findHighestPermissionLevel_teamHighest() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));

        when(subscriptionPermissionGateway.fetchAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setAccountId(accountId)
                        .setSubscriptionId(subscriptionId)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        when(subscriptionPermissionGateway.fetchTeamPermission(teamId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId).block();

        assertNotNull(permissionLevel);
        assertEquals(PermissionLevel.OWNER, permissionLevel);
    }

    @Test
    void findHighestPermissionLevel_accountHighest() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));

        when(subscriptionPermissionGateway.fetchAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setAccountId(accountId)
                        .setSubscriptionId(subscriptionId)
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        when(subscriptionPermissionGateway.fetchTeamPermission(teamId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId).block();

        assertNotNull(permissionLevel);
        assertEquals(PermissionLevel.CONTRIBUTOR, permissionLevel);
    }
}
