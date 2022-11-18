package com.smartsparrow.rtm.message.authorization;

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

import com.smartsparrow.iam.data.permission.plugin.AccountPluginPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.rtm.message.recv.plugin.PluginPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AllowEqualOrHigherPluginPermissionLevelTest {

    @Mock
    private PluginPermissionService pluginPermissionService;

    @InjectMocks
    private AllowEqualOrHigherPluginPermissionLevel authorizer;

    private AuthenticationContext authenticationContext;
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID targetAccountId = UUID.randomUUID();
    private static final UUID targetTeamId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private PluginPermissionMessage accountMessage;
    private PluginPermissionMessage teamMessage;
    private AccountPluginPermission target;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Account account = mock(Account.class);
        authenticationContext = mock(AuthenticationContext.class);
        accountMessage = mock(PluginPermissionMessage.class);
        teamMessage = mock(PluginPermissionMessage.class);
        target = mock(AccountPluginPermission.class);
        when(target.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);

        when(account.getId()).thenReturn(requestingAccountId);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(accountMessage.getPluginId()).thenReturn(pluginId);
        when(accountMessage.getAccountId()).thenReturn(targetAccountId);
        when(teamMessage.getPluginId()).thenReturn(pluginId);
        when(teamMessage.getTeamId()).thenReturn(targetTeamId);

        when(pluginPermissionService.fetchAccountPermission(targetAccountId, pluginId)).thenReturn(Flux.just(target));

    }

    @Test
    void test_targetAccountNotFound() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(pluginPermissionService.fetchAccountPermission(targetAccountId, pluginId)).thenReturn(Flux.empty());
        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_targetTeamNotFound() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(pluginPermissionService.fetchTeamPermission(targetTeamId, pluginId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, teamMessage));
    }

    @Test
    void test_requestingAccountNotFound() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId)).thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestingAccountLowerThanTargetAccount() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(target.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);

        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestingAccountLowerThanTargetTeam() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(pluginPermissionService.fetchTeamPermission(targetTeamId, pluginId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, teamMessage));
    }

    @Test
    void test_requestingAccountEqualTargetAccount() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(target.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestingAccountEqualTargetTeam() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(pluginPermissionService.fetchTeamPermission(targetTeamId, pluginId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, teamMessage));
    }

    @Test
    void test_requestingAccountHigherThanTargetAccount() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        when(target.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestingAccountHigherThanTargetTeam() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        when(pluginPermissionService.fetchTeamPermission(targetTeamId, pluginId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, teamMessage));
    }

    @Test
    void test_withOwnerPermissionLevel() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(accountMessage.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);
        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_withReviewerPermissionLevel() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(accountMessage.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);
        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_withContributorPermissionLevel() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(accountMessage.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_cannotOverrideHigherAccountPermissionLevelOnGranting() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(accountMessage.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(target.getPermissionLevel()).thenReturn(PermissionLevel.OWNER);

        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_canOverrideEqualOrLowerAccountPermissionLevelOnGranting() {
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(teamMessage.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(target.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_cannotOverrideHigherTeamPermissionLevelOnGranting() {
        when(teamMessage.getAccountId()).thenReturn(null);
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(teamMessage.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(pluginPermissionService.fetchTeamPermission(targetTeamId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, teamMessage));
    }

    @Test
    void test_canOverrideHigherTeamPermissionLevelOnGranting() {
        when(teamMessage.getAccountId()).thenReturn(null);
        when(pluginPermissionService.findHighestPermissionLevel(requestingAccountId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(teamMessage.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(pluginPermissionService.fetchTeamPermission(targetTeamId, pluginId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, teamMessage));
    }
}
