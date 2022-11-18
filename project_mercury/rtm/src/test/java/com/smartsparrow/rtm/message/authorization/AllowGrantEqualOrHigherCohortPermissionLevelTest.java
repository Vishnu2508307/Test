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

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.cohort.GrantCohortPermissionMessage;

import reactor.core.publisher.Mono;

class AllowGrantEqualOrHigherCohortPermissionLevelTest {

    @Mock
    private CohortPermissionService cohortPermissionService;

    @InjectMocks
    private AllowGrantEqualOrHigherCohortPermissionLevel authorizer;

    private AuthenticationContext authenticationContext;
    private GrantCohortPermissionMessage message;
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountIdOne = UUID.randomUUID();
    private static final UUID accountIdTwo = UUID.randomUUID();
    private static final UUID teamIdOne = UUID.randomUUID();
    private static final UUID teamIdTwo = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Account requestingAccount = mock(Account.class);
        authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAccount()).thenReturn(requestingAccount);
        when(requestingAccount.getId()).thenReturn(requestingAccountId);
        message = mock(GrantCohortPermissionMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getPermissionLevel()).thenReturn(PermissionLevel.CONTRIBUTOR);

        when(message.getAccountIds()).thenReturn(null);
        when(message.getTeamIds()).thenReturn(null);
    }

    @Test
    @DisplayName("Not Permitted: permission not found for requesting account")
    void test_requestingPermissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied accountId")
    void test_accountIdsPermissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(cohortPermissionService.fetchAccountPermission(accountIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchAccountPermission(accountIdTwo, cohortId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 account of the supplied accountIds has higher permission")
    void test_invalidAccountPermissionFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(cohortPermissionService.fetchAccountPermission(accountIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchAccountPermission(accountIdTwo, cohortId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: it should allow to override if the permission level is the same")
    void test_samePermission() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(cohortPermissionService.fetchAccountPermission(accountIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchAccountPermission(accountIdTwo, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied accountIds have either lower or no permission at all")
    void test_validAccountPermissionFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getAccountIds()).thenReturn(Lists.newArrayList(accountIdOne, accountIdTwo));
        when(cohortPermissionService.fetchAccountPermission(accountIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchAccountPermission(accountIdTwo, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: no permissions found for any of the supplied teamIds")
    @SuppressWarnings("Duplicates")
    void test_teamIdsPermissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));

        when(cohortPermissionService.fetchTeamPermission(teamIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchTeamPermission(teamIdTwo, cohortId)).thenReturn(Mono.empty());

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Not Permitted: at least 1 of the supplied teamIds has higher permission")
    void test_invalidTeamPermissionFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(cohortPermissionService.fetchTeamPermission(teamIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchTeamPermission(teamIdTwo, cohortId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    @DisplayName("Permitted: supplied teamIds have either lower or no permission at all")
    void test_validTeamPermissionFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        when(message.getTeamIds()).thenReturn(Lists.newArrayList(teamIdOne, teamIdTwo));
        when(cohortPermissionService.fetchTeamPermission(teamIdOne, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.fetchTeamPermission(teamIdTwo, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

}
