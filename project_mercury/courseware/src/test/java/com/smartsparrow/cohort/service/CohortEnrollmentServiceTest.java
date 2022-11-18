package com.smartsparrow.cohort.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortEnrollmentGateway;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.payload.CohortEnrollmentPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CohortEnrollmentServiceTest {

    @Mock
    private CohortEnrollmentGateway cohortEnrollmentGateway;

    @Mock
    private AccountService accountService;

    @Mock
    private IESService iesService;

    @InjectMocks
    private CohortEnrollmentService cohortEnrollmentService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(cohortEnrollmentGateway.persistCohortEnrollmentStatus(any(CohortEnrollment.class)))
                .thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void enrollAccount_nullAccountId() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(null, cohortId, EnrollmentType.MANUAL, null));

        assertEquals(t.getMessage(), "accountId is required");
    }

    @Test
    void enrollAccount_nullCohortId() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(accountId, null, EnrollmentType.MANUAL, null));

        assertEquals(t.getMessage(), "cohortId is required");
    }

    @Test
    void enrollAccount_nullEnrollmentType() {
        EnrollmentType enrollmentType = null;
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(accountId, cohortId, enrollmentType, null));

        assertEquals(t.getMessage(), "enrollmentType is required");
    }

    @Test
    void enrollAccount_nullPearsonUid() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(accountId, cohortId, EnrollmentType.OPEN, null));

        assertEquals(t.getMessage(), "pearsonUid is required");
    }

    @Test
    void enrollAccount_success() {
        when(cohortEnrollmentGateway.persist(any(CohortEnrollment.class))).thenReturn(Flux.just(new Void[]{}));

        CohortEnrollment cohortEnrollment = cohortEnrollmentService
                .enrollAccount(accountId, cohortId, EnrollmentType.MANUAL, "iesId").block();

        assertNotNull(cohortEnrollment);
        assertNotNull(cohortEnrollment.getEnrollmentDate());
        assertNotNull(cohortEnrollment.getEnrolledAt());
        assertEquals(accountId, cohortEnrollment.getAccountId());
        assertEquals(cohortId, cohortEnrollment.getCohortId());
        assertEquals("iesId", cohortEnrollment.getPearsonUid());
        assertEquals(EnrollmentType.MANUAL, cohortEnrollment.getEnrollmentType());
        assertNotNull(cohortEnrollment.getEnrollmentDate());
    }

    @Test
    void enrollAccount_fromInstructor_nullAccountId() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(null, cohortId, UUID.randomUUID()));

        assertEquals(t.getMessage(), "accountId is required");
    }

    @Test
    void enrollAccount_fromInstructor_nullCohortId() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(accountId, null, UUID.randomUUID()));

        assertEquals(t.getMessage(), "cohortId is required");
    }

    @Test
    void enrollAccount_fromInstructor_nullInstructorId() {
        UUID enrolledBy = null;
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortEnrollmentService
                .enrollAccount(accountId, cohortId, enrolledBy));

        assertEquals(t.getMessage(), "enrolledBy is required");
    }

    @Test
    void enrollAccount_fromInstructor_idNotFound() {
        UUID enrolledBy = UUID.randomUUID();
        when(accountService.findIESId(accountId)).thenReturn(Mono.empty());
        when(cohortEnrollmentGateway.persist(any(CohortEnrollment.class))).thenReturn(Flux.just(new Void[]{}));

        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                cohortEnrollmentService.enrollAccount(accountId, cohortId, enrolledBy).block());

        assertEquals("pearsonUid not found for account " + accountId, f.getMessage());
    }

    @Test
    void enrollAccount_fromInstructor() {
        UUID enrolledBy = UUID.randomUUID();
        when(accountService.findIESId(accountId)).thenReturn(Mono.just("iesId"));
        when(cohortEnrollmentGateway.persist(any(CohortEnrollment.class))).thenReturn(Flux.just(new Void[]{}));

        CohortEnrollment cohortEnrollment = cohortEnrollmentService
                .enrollAccount(accountId, cohortId, enrolledBy).block();

        assertNotNull(cohortEnrollment);
        assertNotNull(cohortEnrollment.getEnrollmentDate());
        assertNotNull(cohortEnrollment.getEnrolledAt());
        assertEquals(accountId, cohortEnrollment.getAccountId());
        assertEquals(cohortId, cohortEnrollment.getCohortId());
        assertEquals(enrolledBy, cohortEnrollment.getEnrolledBy());
        assertEquals(EnrollmentType.INSTRUCTOR, cohortEnrollment.getEnrollmentType());
        assertEquals("iesId", cohortEnrollment.getPearsonUid());
        assertNotNull(cohortEnrollment.getEnrollmentDate());
    }

    @Test
    void fetchEnrollments_noEnrollmentsFound() {
        when(cohortEnrollmentGateway.findCohortEnrollments(cohortId)).thenReturn(Flux.empty());

        List<CohortEnrollment> all = cohortEnrollmentService.fetchEnrollments(cohortId).collectList().block();

        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void fetchEnrollments_noIdentityFoundForOneEnrollment() {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        UUID accountIdThree = UUID.randomUUID();

        CohortEnrollment one = buildCohortEnrollment(cohortId, accountIdOne);
        CohortEnrollment two = buildCohortEnrollment(cohortId, accountIdTwo);
        CohortEnrollment three = buildCohortEnrollment(cohortId, accountIdThree);

        when(cohortEnrollmentGateway.findCohortEnrollments(cohortId)).thenReturn(Flux.just(one, two, three));

        List<CohortEnrollment> all = cohortEnrollmentService.fetchEnrollments(cohortId).collectList().block();

        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertEquals(3, all.size());

        final CohortEnrollment first = all.get(0);
        final CohortEnrollment second = all.get(1);
        final CohortEnrollment third = all.get(2);

        verifyCohortEnrollment(accountIdOne, first);
        verifyCohortEnrollment(accountIdTwo, second);
        verifyCohortEnrollment(accountIdThree, third);
    }

    @Test
    void getCohortEnrollmentPayload() {
        UUID accountId = UUID.randomUUID();
        CohortEnrollment cohortEnrollment = buildCohortEnrollment(cohortId, accountId);
        AccountAdapter adapter = buildAccountAdapter(accountId, "Alice", "Dev");
        Account account = mock(Account.class);

        when(account.getId()).thenReturn(accountId);
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));
        when(accountService.getPartialAccountAdapter(account)).thenReturn(Mono.just(adapter));


        CohortEnrollmentPayload payload = cohortEnrollmentService.getCohortEnrollmentPayload(cohortEnrollment).block();

        assertNotNull(payload);
        assertEquals(DateFormat.asRFC1123(cohortEnrollment.getEnrollmentDate()), payload.getEnrolledAt());
        assertEquals(cohortId, payload.getCohortId());
        assertEquals(EnrollmentType.MANUAL, payload.getEnrollmentType());

        AccountSummaryPayload summaryPayload = payload.getAccountSummaryPayload();
        assertNotNull(summaryPayload);
        assertEquals(accountId, summaryPayload.getAccountId());
        assertEquals("Alice", summaryPayload.getGivenName());
        assertEquals("Dev", summaryPayload.getFamilyName());
        assertEquals(String.format("%s@dev.dev", summaryPayload.getAccountId()), summaryPayload.getPrimaryEmail());
    }

    @Test
    void getCohortEnrollmentPayload_emptyObjectWhenAttributesNotFound() {
        UUID accountId = UUID.randomUUID();
        CohortEnrollment cohortEnrollment = buildCohortEnrollment(cohortId, accountId);
        AccountAdapter adapter = new AccountAdapter()
                .setIdentityAttributes(new AccountIdentityAttributes())
                .setAvatars(Lists.newArrayList(new AccountAvatar()));
        Account account = mock(Account.class);

        when(account.getId()).thenReturn(accountId);
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));
        when(accountService.getPartialAccountAdapter(account)).thenReturn(Mono.just(adapter));


        CohortEnrollmentPayload payload = cohortEnrollmentService.getCohortEnrollmentPayload(cohortEnrollment).block();

        assertNotNull(payload);
        assertEquals(DateFormat.asRFC1123(cohortEnrollment.getEnrollmentDate()), payload.getEnrolledAt());
        assertEquals(cohortId, payload.getCohortId());
        assertEquals(EnrollmentType.MANUAL, payload.getEnrollmentType());

        AccountSummaryPayload summaryPayload = payload.getAccountSummaryPayload();
        assertNotNull(summaryPayload);
        assertNotNull(summaryPayload.getAccountId());
        assertEquals(accountId, summaryPayload.getAccountId());
        assertNull(summaryPayload.getGivenName());
        assertNull(summaryPayload.getFamilyName());
        assertNull(summaryPayload.getPrimaryEmail());
    }

    @Test
    void disenrollAccount_noCohortId() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> cohortEnrollmentService.disenrollAccount(accountId, null));

        assertEquals("cohortId is required", t.getMessage());
    }

    @Test
    void disenrollAccount_noAccountId() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> cohortEnrollmentService.disenrollAccount(null, cohortId));

        assertEquals("accountId is required", t.getMessage());
    }

    @Test
    void disenrollAccount() {
        ArgumentCaptor<CohortEnrollment> cohortEnrollmentCaptor = ArgumentCaptor.forClass(CohortEnrollment.class);
        when(cohortEnrollmentGateway.delete(any(CohortEnrollment.class))).thenReturn(Flux.just(new Void[]{}));

        cohortEnrollmentService.disenrollAccount(accountId, cohortId);
        verify(cohortEnrollmentGateway, atLeastOnce()).delete(cohortEnrollmentCaptor.capture());

        CohortEnrollment captured = cohortEnrollmentCaptor.getValue();

        assertEquals(accountId, captured.getAccountId());
        assertEquals(cohortId, captured.getCohortId());
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchCohortEnrollments() {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        UUID accountIdThree = UUID.randomUUID();

        CohortEnrollment one = buildCohortEnrollment(cohortId, accountIdOne);
        CohortEnrollment two = buildCohortEnrollment(cohortId, accountIdTwo);
        CohortEnrollment three = buildCohortEnrollment(cohortId, accountIdThree);
        final AccountSummaryPayload summaryOne = new AccountSummaryPayload()
                .setAccountId(accountIdOne)
                .setPrimaryEmail("vale@dev.dev")
                .setFamilyName("Rossi")
                .setGivenName("Vale");
        final AccountSummaryPayload summaryTwo = new AccountSummaryPayload()
                .setAccountId(accountIdTwo)
                .setPrimaryEmail("casey@dev.dev")
                .setGivenName("Stoner")
                .setFamilyName("Casey");
        final AccountSummaryPayload summaryThree = new AccountSummaryPayload()
                .setAccountId(accountIdThree);

        when(cohortEnrollmentGateway.findCohortEnrollments(cohortId)).thenReturn(Flux.just(one, two, three));
        when(iesService.getAccountSummaryPayload(any(List.class)))
                .thenReturn(Flux.just(summaryOne, summaryTwo, summaryThree));

        final List<CohortEnrollmentPayload> enrollments = cohortEnrollmentService.fetchCohortEnrollments(cohortId)
                .collectList()
                .block();

        assertNotNull(enrollments);
        assertEquals(3, enrollments.size());

        final CohortEnrollmentPayload first = enrollments.get(0);
        final CohortEnrollmentPayload second = enrollments.get(1);
        final CohortEnrollmentPayload third = enrollments.get(2);

        assertNotNull(first);
        assertEquals(one.getCohortId(), first.getCohortId());
        assertEquals(one.getEnrolledAt(), first.getEnrolledAt());
        assertEquals(one.getEnrollmentType(), first.getEnrollmentType());
        assertEquals(summaryOne, first.getAccountSummaryPayload());
        assertNotNull(second);
        assertEquals(two.getCohortId(), second.getCohortId());
        assertEquals(two.getEnrolledAt(), second.getEnrolledAt());
        assertEquals(two.getEnrollmentType(), second.getEnrollmentType());
        assertEquals(summaryTwo, second.getAccountSummaryPayload());
        assertNotNull(third);
        assertEquals(three.getCohortId(), third.getCohortId());
        assertEquals(three.getEnrolledAt(), third.getEnrolledAt());
        assertEquals(three.getEnrollmentType(), third.getEnrollmentType());
        assertEquals(summaryThree, third.getAccountSummaryPayload());
    }

    private void verifyCohortEnrollment(UUID accountIdThree, CohortEnrollment third) {
        assertAll(() -> {
            assertNotNull(third);
            assertEquals(cohortId, third.getCohortId());
            assertNotNull(third.getEnrollmentDate());
            assertEquals(EnrollmentType.MANUAL, third.getEnrollmentType());
            assertEquals(accountIdThree, third.getAccountId());
        });
    }

    private AccountAdapter buildAccountAdapter(UUID accountId, String givenName, String familyName) {

        return new AccountAdapter()
                .setIdentityAttributes(new AccountIdentityAttributes()
                        .setFamilyName(familyName)
                        .setGivenName(givenName)
                        .setPrimaryEmail(String.format("%s@dev.dev", accountId))
                        .setAccountId(accountId))
                .setAvatars(Lists.newArrayList(new AccountAvatar()));
    }

    private CohortEnrollment buildCohortEnrollment(UUID cohortId, UUID accountId) {
        final UUID date = UUIDs.timeBased();
        return new CohortEnrollment()
                .setPearsonUid(String.format("%s_%s", accountId.toString(), "uid"))
                .setEnrollmentType(EnrollmentType.MANUAL)
                .setCohortId(cohortId)
                .setAccountId(accountId)
                .setEnrolledAt(DateFormat.asRFC1123(date))
                .setEnrollmentDate(date);
    }
}
