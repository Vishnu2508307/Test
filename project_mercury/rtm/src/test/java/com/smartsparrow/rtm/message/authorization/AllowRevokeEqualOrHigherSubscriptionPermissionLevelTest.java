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

import com.smartsparrow.iam.data.permission.subscription.AccountSubscriptionPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.recv.iam.RevokeSubscriptionPermissionMessage;

import reactor.core.publisher.Mono;

class AllowRevokeEqualOrHigherSubscriptionPermissionLevelTest {

    @InjectMocks
    private AllowRevokeEqualOrHigherSubscriptionPermissionLevel authorizer;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private AuthenticationContext authenticationContext;
    private RevokeSubscriptionPermissionMessage message;
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID requestingAccountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        message = mock(RevokeSubscriptionPermissionMessage.class);

        Account account = mock(Account.class);

        when(account.getId()).thenReturn(requestingAccountId);
        when(authenticationContext.getAccount()).thenReturn(account);

        when(message.getSubscriptionId()).thenReturn(subscriptionId);
        when(message.getAccountId()).thenReturn(null);
        when(message.getTeamId()).thenReturn(null);

        when(subscriptionPermissionService.findHighestPermissionLevel(account.getId(), subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
    }

    @Test
    void test_requestingPermissionNotFound() {
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_accountAndTeamNull() {
        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_targetAccountPermissionLower() {
        when(message.getAccountId()).thenReturn(accountId);

        when(subscriptionPermissionService.findAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setPermissionLevel(PermissionLevel.REVIEWER)));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_targetAccountPermissionEqual() {
        when(message.getAccountId()).thenReturn(accountId);

        when(subscriptionPermissionService.findAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_targetAccountPermissionHigher(){
        when(message.getAccountId()).thenReturn(accountId);

        when(subscriptionPermissionService.findAccountPermission(accountId, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setPermissionLevel(PermissionLevel.OWNER)));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_targetTeamPermissionLower() {
        when(message.getTeamId()).thenReturn(teamId);

        when(subscriptionPermissionService.findTeamPermission(teamId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_targetTeamPermissionEqual() {
        when(message.getTeamId()).thenReturn(teamId);

        when(subscriptionPermissionService.findTeamPermission(teamId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_targetTeamPermissionHigher() {
        when(message.getTeamId()).thenReturn(teamId);

        when(subscriptionPermissionService.findTeamPermission(teamId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_bothAccountAndTeamSupplied() {
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getTeamId()).thenReturn(teamId);

        assertFalse(authorizer.test(authenticationContext, message));
    }

}
