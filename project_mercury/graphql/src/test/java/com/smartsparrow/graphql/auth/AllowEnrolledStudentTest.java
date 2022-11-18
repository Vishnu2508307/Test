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
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AllowEnrolledStudentTest {

    @InjectMocks
    private AllowEnrolledStudent allowEnrolledStudent;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;
    @Mock
    private CohortEnrollmentService cohortEnrollmentService;

    @Mock
    private CohortService cohortService;

    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT));
        when(cohortService.findCohortInstanceIds(cohortId)).thenReturn(Flux.empty());
    }

    @Test
    @DisplayName("Should fail when cohortId is not supplies")
    void test_CohortNotProvided() {
        assertThrows(IllegalArgumentFault.class, () -> allowEnrolledStudent.test(authenticationContext,null));
    }

//    @Test
//    @DisplayName("User without student role should not be permitted")
//    void test_instructor() {
//        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.INSTRUCTOR));
//
//        assertFalse(allowEnrolledStudent.test(cohortId));
//    }

    @Test
    @DisplayName("Not enrolled student should not be permitted")
    void test_student_notEnrolled() {
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId)).thenReturn(Mono.empty());

        assertFalse(allowEnrolledStudent.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Enrolled student should be allowed")
    void test_student_enrolled() {
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId)).thenReturn(Mono.just(new CohortEnrollment()));

        assertTrue(allowEnrolledStudent.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Enrolled LTI student should be allowed - single LTI cohort instance")
    void test_ltiStudent_singleEnrolled() {
        UUID cohortInstanceId1 = UUID.randomUUID();
        UUID cohortInstanceId2 = UUID.randomUUID();

        when(cohortService.findCohortInstanceIds(cohortId)).thenReturn(Flux.just(cohortInstanceId1, cohortInstanceId2));
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId)).thenReturn(Mono.empty());
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortInstanceId1))
                .thenReturn(Mono.empty());
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortInstanceId2))
                .thenReturn(Mono.just(new CohortEnrollment().setCohortId(cohortInstanceId2)));

        assertTrue(allowEnrolledStudent.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Enrolled LTI student should be allowed - 2 LTI cohort instances")
    void test_ltiStudent_multipleEnrolled() {
        UUID cohortInstanceId1 = UUID.randomUUID();
        UUID cohortInstanceId2 = UUID.randomUUID();

        when(cohortService.findCohortInstanceIds(cohortId)).thenReturn(Flux.just(cohortInstanceId1, cohortInstanceId2));
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId)).thenReturn(Mono.empty());
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortInstanceId1))
                .thenReturn(Mono.just(new CohortEnrollment().setCohortId(cohortInstanceId1)));
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortInstanceId2))
                .thenReturn(Mono.just(new CohortEnrollment().setCohortId(cohortInstanceId2)));

        assertTrue(allowEnrolledStudent.test(authenticationContext,cohortId));
    }

    @Test
    @DisplayName("Not enrolled LTI student should not be permitted")
    void test_ltiStudent_noEnrolled() {
        UUID cohortInstanceId1 = UUID.randomUUID();

        when(cohortService.findCohortInstanceIds(cohortId)).thenReturn(Flux.just(cohortInstanceId1));
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId)).thenReturn(Mono.empty());
        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortInstanceId1))
                .thenReturn(Mono.empty());

        assertFalse(allowEnrolledStudent.test(authenticationContext,cohortId));
    }
}
