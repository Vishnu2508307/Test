package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.message.recv.team.TeamPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AllowEqualOrHigherTeamPermissionLevelTest {

    @InjectMocks
    private AllowEqualOrHigherTeamPermissionLevel authorizer;

    @Mock
    private TeamService teamService;

    private static final UUID teamId = UUID.randomUUID();
    private static final UUID targetAccountId = UUID.randomUUID();
    private static final UUID requesterAccountId = UUID.randomUUID();
    private TeamPermission targetPermission;
    private AuthenticationContext authenticationContext;
    private TeamPermissionMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(TeamPermissionMessage.class);
        authenticationContext = mock(AuthenticationContext.class);
        TeamPermission requesterPermission = mock(TeamPermission.class);
        targetPermission = mock(TeamPermission.class);
        Account account = mock(Account.class);

        when(account.getId()).thenReturn(requesterAccountId);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(message.getTeamId()).thenReturn(teamId);
        when(message.getAccountIds()).thenReturn(Arrays.asList(targetAccountId));
        when(message.getPermissionLevel()).thenReturn(null);

        when(teamService.fetchPermission(requesterAccountId, teamId))
                .thenReturn(Mono.just(requesterPermission));

        when(teamService.fetchPermission(targetAccountId, teamId))
                .thenReturn(Mono.just(targetPermission));

        when(requesterPermission.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
    }

    @Test
    void test_requesterPermissionNotFound() {
        when(teamService.fetchPermission(requesterAccountId, teamId))
                .thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_grantPermission() {
        when(teamService.fetchPermissions(Arrays.asList(targetAccountId), teamId)).thenReturn(Flux.empty());
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_grantPermissionCanOverride() {
        when(teamService.fetchPermissions(Arrays.asList(targetAccountId), teamId)).thenReturn(Flux.empty());
        when(targetPermission.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_grantPermissionCannotOverride() {
        when(teamService.fetchPermissions(Arrays.asList(targetAccountId), teamId)).thenReturn(
                Flux.just(new TeamPermission()
        .setAccountId(targetAccountId)
        .setTeamId(teamId)
        .setPermissionLevel(PermissionLevel.OWNER)));
        when(targetPermission.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_revokeNonExistingPermission() {
        when(teamService.fetchPermission(requesterAccountId, teamId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_revokeHigherPermission() {
        when(teamService.fetchPermissions(Arrays.asList(targetAccountId), teamId)).thenReturn(
                Flux.just(new TeamPermission()
                        .setAccountId(targetAccountId)
                        .setTeamId(teamId)
                        .setPermissionLevel(PermissionLevel.OWNER)));
        when(targetPermission.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_revokeLowerPermission() {
        when(teamService.fetchPermissions(Arrays.asList(targetAccountId), teamId)).thenReturn(
                Flux.just(new TeamPermission()
                        .setAccountId(targetAccountId)
                        .setTeamId(teamId)
                        .setPermissionLevel(PermissionLevel.REVIEWER)));
        when(targetPermission.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);

        assertTrue(authorizer.test(authenticationContext, message));
    }
}
