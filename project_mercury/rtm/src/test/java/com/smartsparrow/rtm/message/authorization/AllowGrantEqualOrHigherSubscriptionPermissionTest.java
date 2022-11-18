package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.subscription.AccountSubscriptionPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.recv.iam.GrantSubscriptionPermissionMessage;

import reactor.core.publisher.Mono;

class AllowGrantEqualOrHigherSubscriptionPermissionTest {

    @InjectMocks
    private AllowGrantEqualOrHigherSubscriptionPermission authorizer;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private GrantSubscriptionPermissionMessage message;
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID accountIdOne = UUID.randomUUID();
    private static final UUID accountIdTwo = UUID.randomUUID();
    private static final UUID teamIdOne = UUID.randomUUID();
    private static final UUID teamIdTwo = UUID.randomUUID();
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(GrantSubscriptionPermissionMessage.class);
        authenticationContext = mock(AuthenticationContext.class);

        when(message.getSubscriptionId()).thenReturn(subscriptionId);
        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(requestingAccountId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    @DisplayName("Not Permitted: permission not found for requesting account")
    void test_requestingPermissionNotFound() {
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied accountIds")
    void test_accountIdsPermissionsNotFound() {
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(subscriptionPermissionService.findAccountPermission(accountIdOne, subscriptionId)).thenReturn(Mono.empty());
        when(subscriptionPermissionService.findAccountPermission(accountIdTwo, subscriptionId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied accountIds has higher permission")
    void test_invalidAccountPermissionsFound() {
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(subscriptionPermissionService.findAccountPermission(accountIdOne, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setAccountId(accountIdOne)
                        .setPermissionLevel(PermissionLevel.OWNER)));
        when(subscriptionPermissionService.findAccountPermission(accountIdTwo, subscriptionId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied accountIds have either lower or no permission at all")
    void test_validAccountPermissionsFound() {
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(subscriptionPermissionService.findAccountPermission(accountIdOne, subscriptionId))
                .thenReturn(Mono.just(new AccountSubscriptionPermission()
                        .setAccountId(accountIdOne)
                        .setPermissionLevel(PermissionLevel.REVIEWER)));
        when(subscriptionPermissionService.findAccountPermission(accountIdTwo, subscriptionId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied teamIds")
    void test_teamIdsPermissionsNotFound() {
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(subscriptionPermissionService.findTeamPermission(teamIdOne, subscriptionId)).thenReturn(Mono.empty());
        when(subscriptionPermissionService.findTeamPermission(teamIdTwo, subscriptionId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied teamIds has higher permission")
    void test_invalidTeamIdPermissionFound() {
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(subscriptionPermissionService.findTeamPermission(teamIdOne, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        when(subscriptionPermissionService.findTeamPermission(teamIdTwo, subscriptionId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied teamIds have either lower or no permission at all")
    void test_validTeamPermissionFound() {
        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(subscriptionPermissionService.findHighestPermissionLevel(requestingAccountId, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(subscriptionPermissionService.findTeamPermission(teamIdOne, subscriptionId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        when(subscriptionPermissionService.findTeamPermission(teamIdTwo, subscriptionId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

}
