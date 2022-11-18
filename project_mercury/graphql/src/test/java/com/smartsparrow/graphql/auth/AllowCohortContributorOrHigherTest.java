package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import reactor.core.publisher.Mono;

class AllowCohortContributorOrHigherTest {

    @InjectMocks
    private AllowCohortContributorOrHigher allowCohortContributorOrHigher;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;
    @Mock
    private CohortPermissionService cohortPermissionService;

    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    @DisplayName("should fail when cohortId is not supplies")
    void test_CohortNotProvided() {
        assertThrows(IllegalArgumentFault.class, () -> allowCohortContributorOrHigher.test(authenticationContext,null));
    }

    @Test
    @DisplayName("should fail if authenticated account does not have roles")
    void test_NoRoles() {
        when(account.getRoles()).thenReturn(null);
        assertThrows(IllegalArgumentFault.class, () -> allowCohortContributorOrHigher.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("User with only student role should not have permissions")
    void test_student_noPermission() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT));

        assertFalse(allowCohortContributorOrHigher.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Instructor without permissions over cohort should not be allowed")
    void test_instructor_noPermission() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.INSTRUCTOR));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.empty());

        assertFalse(allowCohortContributorOrHigher.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Instructor with permission level contributor over cohort should be allowed")
    void test_instructor_withPermissionContributor() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(allowCohortContributorOrHigher.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Instructor with permission level owner over cohort should be allowed")
    void test_instructor_withPermissionOwner() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(allowCohortContributorOrHigher.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Instructor with permission level reviewer over cohort should not be allowed")
    void test_instructorAndStudent_withPermissionReviewer() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.INSTRUCTOR, AccountRole.STUDENT));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(allowCohortContributorOrHigher.test(authenticationContext,cohortId));
    }
}
