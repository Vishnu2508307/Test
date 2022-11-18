package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AllowCohortInstructorTest {

    @InjectMocks
    private AllowCohortInstructor allowCohortInstructor;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;
    @Mock
    private CohortPermissionService cohortPermissionService;

    @Mock
    private CohortService cohortService;

    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);

        when(cohortService.findCohortInstanceIds(cohortId)).thenReturn(Flux.empty());
    }

    @Test
    @DisplayName("should fail when cohortId is not supplies")
    void test_CohortNotProvided() {
        assertThrows(IllegalArgumentFault.class, () -> allowCohortInstructor.test(authenticationContext,null));
    }

    @Test
    @DisplayName("should fail if authenticated account does not have roles")
    void test_NoRoles() {
        when(account.getRoles()).thenReturn(null);
        assertThrows(IllegalArgumentFault.class, () -> allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("User with only student role should not have permissions")
    void test_student_noPermission() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT));

        assertFalse(allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Instructor without permissions over cohort should not be allowed")
    void test_instructor_noPermission() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.INSTRUCTOR));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.empty());

        assertFalse(allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @ParameterizedTest
    @EnumSource(PermissionLevel.class)
    @DisplayName("Instructor with permission level over cohort should be allowed")
    void test_instructor_withPermission(PermissionLevel level) {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.just(level));

        assertTrue(allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @ParameterizedTest
    @EnumSource(PermissionLevel.class)
    @DisplayName("Instructor with permission level over LTI cohort should be allowed")
    void test_instructor_withPermission_withLtiCohortInstance(PermissionLevel level) {
        UUID cohortInstance1 = UUID.randomUUID();
        UUID cohortInstance2 = UUID.randomUUID();

        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR));

        when(cohortService.findCohortInstanceIds(cohortId)).thenReturn(Flux.just(cohortInstance1, cohortInstance2));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortInstance1)).thenReturn(Mono.empty());
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortInstance2)).thenReturn(Mono.just(level));

        assertTrue(allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Instructor without permissions over LTI cohort should not be allowed")
    void test_instructor_noPermission_withLtiCohortInstance() {
        UUID cohortInstance1 = UUID.randomUUID();

        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortInstance1)).thenReturn(Mono.empty());

        assertFalse(allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("User with student and aero instructor role should be allowed")
    void test_aeroInstructorAndStudent_withPermission() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR, AccountRole.STUDENT));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(allowCohortInstructor.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("User with student and aero instructor role should not be allowed")
    void test_instructorAndStudent_withPermission() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.INSTRUCTOR, AccountRole.STUDENT));
        when(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(allowCohortInstructor.test(authenticationContext,cohortId));
    }
}
