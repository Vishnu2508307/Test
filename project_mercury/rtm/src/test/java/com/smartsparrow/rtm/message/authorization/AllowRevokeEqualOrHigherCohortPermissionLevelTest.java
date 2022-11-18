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

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.cohort.RevokeCohortPermissionMessage;

import reactor.core.publisher.Mono;

class AllowRevokeEqualOrHigherCohortPermissionLevelTest {

    @Mock
    private CohortPermissionService cohortPermissionService;

    @InjectMocks
    private AllowRevokeEqualOrHigherCohortPermissionLevel authorizer;
    @Mock
    private RevokeCohortPermissionMessage accountMessage;
    @Mock
    private RevokeCohortPermissionMessage teamMessage;

    private AuthenticationContext authenticationContext;
    private static final UUID targetAccountId = UUID.randomUUID();
    private static final UUID targetTeamId = UUID.randomUUID();
    private static final UUID requestingAccountId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Account requestingAccount = mock(Account.class);
        authenticationContext = mock(AuthenticationContext.class);
        when(requestingAccount.getId()).thenReturn(requestingAccountId);

        when(accountMessage.getCohortId()).thenReturn(cohortId);
        when(accountMessage.getAccountId()).thenReturn(targetAccountId);
        when(accountMessage.getTeamId()).thenReturn(null);
        when(teamMessage.getCohortId()).thenReturn(cohortId);
        when(teamMessage.getTeamId()).thenReturn(targetTeamId);
        when(teamMessage.getAccountId()).thenReturn(null);

        when(authenticationContext.getAccount()).thenReturn(requestingAccount);

        when(cohortPermissionService.fetchAccountPermission(targetAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

    }

    @Test
    void test_samePermissionLevel() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestingHigherThanTarget() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestingLowerThanTarget() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_targetPermissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(cohortPermissionService.fetchAccountPermission(targetAccountId, cohortId)).thenReturn(Mono.empty());
        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_requestPermissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.empty());
        when(cohortPermissionService.fetchAccountPermission(targetAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertFalse(authorizer.test(authenticationContext, accountMessage));
    }

    @Test
    void test_revokeTeamPermission() {
        when(cohortPermissionService.findHighestPermissionLevel(requestingAccountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        when(cohortPermissionService.fetchTeamPermission(targetTeamId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertTrue(authorizer.test(authenticationContext, teamMessage));
    }

}
