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

class AllowCohortReviewerOrHigherTest {

    @Mock
    private CohortPermissionService cohortPermissionService;

    @InjectMocks
    private AllowCohortReviewerOrHigher allowCohortReviewerOrHigher;

    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    private CohortMessage cohortMessage;
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Account account = mock(Account.class);
        authenticationContext = mock(AuthenticationContext.class);
        cohortMessage = mock(CohortMessage.class);

        when(account.getId()).thenReturn(accountId);
        when(cohortMessage.getCohortId()).thenReturn(cohortId);

        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_permissionNotFound() {
        when(cohortPermissionService.findHighestPermissionLevel(accountId, cohortId))
                .thenReturn(Mono.empty());

        assertFalse(allowCohortReviewerOrHigher.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_cohortIdNotSupplied() {
        when(cohortMessage.getCohortId()).thenReturn(null);

        assertFalse(allowCohortReviewerOrHigher.test(authenticationContext, cohortMessage));
    }

    @Test
    void test_success() {
        assertTrue(allowCohortReviewerOrHigher.test(authenticationContext, cohortMessage));
    }

}
