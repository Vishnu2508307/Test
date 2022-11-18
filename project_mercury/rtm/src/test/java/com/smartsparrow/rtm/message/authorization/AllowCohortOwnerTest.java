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
import com.smartsparrow.rtm.message.recv.cohort.CohortMessage;

import reactor.core.publisher.Mono;

class AllowCohortOwnerTest {

    @Mock
    private CohortPermissionService cohortPermissionService;

    @InjectMocks
    private AllowCohortOwner authorizer;

    private CohortMessage cohortMessage;
    private AuthenticationContext authenticationContext;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        cohortMessage = mock(CohortMessage.class);
        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);

        when(cohortMessage.getCohortId()).thenReturn(cohortId);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
    }

    @Test
    void test_permissionLevelNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_reviewerNotAuthorized() {
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertFalse(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_contributorNotAuthorized() {
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));
        assertFalse(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_ownerAuthorized() {
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));
        assertTrue(authorizer.test(authenticationContext, cohortMessage));
    }

}
