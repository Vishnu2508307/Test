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

class AllowCohortContributorOrHigherTest {

    @Mock
    private CohortPermissionService cohortPermissionService;

    @InjectMocks
    private AllowCohortContributorOrHigher authorizer;

    private AuthenticationContext authenticationContext;
    private Account account;
    private CohortMessage cohortMessage;
    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mock(AuthenticationContext.class);
        account = mock(Account.class);
        cohortMessage = mock(CohortMessage.class);

        when(account.getId()).thenReturn(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        when(cohortMessage.getCohortId()).thenReturn(cohortId);
    }

    @Test
    void test_cohortIdNotSupplied() {
        when(cohortMessage.getCohortId()).thenReturn(null);

        assertFalse(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_permissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_permissionReviewer() {
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_permissionContributor() {
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(authorizer.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_permissionOwner() {
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(authorizer.test(authenticationContext, cohortMessage));
    }

}
