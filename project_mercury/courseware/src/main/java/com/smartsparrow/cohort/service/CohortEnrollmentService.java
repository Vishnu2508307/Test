package com.smartsparrow.cohort.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Function;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortEnrollmentGateway;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.data.HistoricalCohortEnrollment;
import com.smartsparrow.cohort.payload.CohortEnrollmentPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortEnrollmentService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CohortEnrollmentService.class);

    private final CohortEnrollmentGateway cohortEnrollmentGateway;
    private final AccountService accountService;
    private final IESService iesService;

    @Inject
    public CohortEnrollmentService(final CohortEnrollmentGateway cohortEnrollmentGateway,
                                   final AccountService accountService,
                                   final IESService iesService) {
        this.cohortEnrollmentGateway = cohortEnrollmentGateway;
        this.accountService = accountService;
        this.iesService = iesService;
    }

    /**
     * Enroll an account in a cohort
     *
     * @param accountId the account id to enroll
     * @param cohortId the cohort to enroll the account in
     * @param enrollmentType the type of enrollment
     * @param pearsonUid the ies user id
     * @return a {@link Mono} of {@link CohortEnrollment}
     */
    @Trace(async = true)
    public Mono<CohortEnrollment> enrollAccount(final UUID accountId, final UUID cohortId, final EnrollmentType enrollmentType,
                                                final String pearsonUid) {

        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(cohortId != null, "cohortId is required");
        affirmArgument(enrollmentType != null, "enrollmentType is required");
        affirmArgument(pearsonUid != null, "pearsonUid is required");

        UUID id = UUIDs.timeBased();

        CohortEnrollment cohortEnrollment = new CohortEnrollment()
                .setCohortId(cohortId)
                .setAccountId(accountId)
                .setEnrollmentType(enrollmentType)
                .setEnrollmentDate(id)
                .setPearsonUid(pearsonUid)
                .setEnrolledAt(DateFormat.asRFC1123(id));

        return cohortEnrollmentGateway.persist(cohortEnrollment)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(cohortEnrollment));
    }

    /**
     * Find a cohort enrollment for an account
     *
     * @param accountId the account to find the enrolment for
     * @param cohortId the cohort the account is enrolled to
     * @return a mono with the cohort enrollment or an empty mono when not found
     */
    public Mono<CohortEnrollment> findCohortEnrollment(final UUID accountId, final UUID cohortId) {
        return cohortEnrollmentGateway.findAccountEnrollment(accountId, cohortId);
    }

    /**
     * Allows an instructor to enroll an account in a cohort
     *
     * @param accountId the account to enrol
     * @param cohortId the cohort to enrol the account to
     * @param enrolledBy the account id of the instructor performing the enrol action
     * @return a mono of cohort enrollment
     * @throws IllegalArgumentFault when any of the required argument is not supplied
     * @throws NotFoundFault when the pearsonUid is not found for this account
     */
    public Mono<CohortEnrollment> enrollAccount(final UUID accountId, final UUID cohortId, final UUID enrolledBy) {
        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(cohortId != null, "cohortId is required");
        affirmArgument(enrolledBy != null, "enrolledBy is required");

        final UUID id = UUIDs.timeBased();

        return accountService.findIESId(accountId)
                .single()
                // log an error when the pearsonUid is not found
                .doOnEach(log.reactiveErrorThrowableIf("error enrolling account", (Function<Throwable, Boolean>) input -> input instanceof NoSuchElementException))
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault("pearsonUid not found for account " + accountId);
                })
                .flatMap(pearsonUid -> {
                    final CohortEnrollment cohortEnrollment = new CohortEnrollment()
                            .setCohortId(cohortId)
                            .setAccountId(accountId)
                            .setEnrollmentType(EnrollmentType.INSTRUCTOR)
                            .setEnrollmentDate(id)
                            .setEnrolledAt(DateFormat.asRFC1123(id))
                            .setPearsonUid(pearsonUid)
                            .setEnrolledBy(enrolledBy);

                    return cohortEnrollmentGateway.persist(cohortEnrollment)
                            .then(Mono.just(cohortEnrollment));
                });
    }

    /**
     * Disenroll an account from a cohort
     *
     * @param accountId the account to disenroll
     * @param cohortId the cohort id to disernoll the account from
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> disenrollAccount(final UUID accountId, final UUID cohortId) {
        checkArgument(accountId != null, "accountId is required");
        checkArgument(cohortId != null, "cohortId is required");

        final UUID id = UUIDs.timeBased();

        CohortEnrollment cohortEnrollment = new CohortEnrollment()
                .setEnrollmentDate(id)
                .setEnrollmentType(EnrollmentType.UNENROLLED)
                .setAccountId(accountId)
                .setCohortId(cohortId)
                .setEnrolledBy(null) // TODO set the account responsible for this status
                .setEnrolledAt(DateFormat.asRFC1123(id));

        // delete the cohort enrollment from the access tables
        return cohortEnrollmentGateway.delete(cohortEnrollment)
                // persist the cohort enrollment to the cohort enrollment status table
                .thenMany(cohortEnrollmentGateway.persistCohortEnrollmentStatus(cohortEnrollment));
    }

    /**
     * Build a cohort enrollment payload from a cohort enrollment argument
     *
     * @param cohortEnrollment the cohort enrollment to build the payload for
     * @return a {@link Mono} of {@link CohortEnrollmentPayload}
     */
    public Mono<CohortEnrollmentPayload> getCohortEnrollmentPayload(@Nonnull final CohortEnrollment cohortEnrollment) {
        return getCohortEnrollmentPayload(cohortEnrollment, accountService
                .findById(cohortEnrollment.getAccountId()).singleOrEmpty());
    }

    /**
     * Fetch a list of all the cohort enrollment for a cohort.
     *
     * @param cohortId the cohort id to fetch the enrollment for
     * @return a {@link Flux} of {@link CohortEnrollment}
     */
    public Flux<CohortEnrollment> fetchEnrollments(final UUID cohortId) {
        return cohortEnrollmentGateway.findCohortEnrollments(cohortId);
    }

    /**
     * Fetch cohort enrollment payloads for a cohort. Performs an external call to the IES service to bulk fetch
     * account identity information for the enrolled accounts. (This is an expensive call)
     *
     * @param cohortId the cohort id to fetch the enrolled accounts detail
     * @return a flux {@link CohortEnrollmentPayload}
     */
    public Flux<CohortEnrollmentPayload> fetchCohortEnrollments(final UUID cohortId) {
        // find the accounts enrolled to this cohort
        return fetchEnrollments(cohortId)
                .collectList()
                .flux()
                .flatMap(cohortEnrollments -> {
                    // prepare all the IES accounts we have to fetch the details for
                    final List<IESAccountTracking> accounts = cohortEnrollments.stream()
                            .map(cohortEnrollment -> new IESAccountTracking()
                                    .setAccountId(cohortEnrollment.getAccountId())
                                    .setIesUserId(cohortEnrollment.getPearsonUid()))
                            .collect(Collectors.toList());

                    // keep the enrollments in a map so they can quickly be accessed later
                    final Map<UUID, CohortEnrollment> enrollmentMap = cohortEnrollments.stream()
                            .collect(Collectors.toMap(CohortEnrollment::getAccountId, cohortEnrollment -> cohortEnrollment));

                    // get the account summary details for the enrolled accounts via IES
                    return iesService.getAccountSummaryPayload(accounts)
                            // for each one build a cohort enrollment payload
                            .map(accountSummaryPayload -> CohortEnrollmentPayload.from(enrollmentMap.get(accountSummaryPayload.getAccountId()), accountSummaryPayload));
                });
    }

    /**
     * Fetch all the currently enrolled accounts for a cohort
     *
     * @param cohortId the cohort to find the enrolled accounts for
     * @return a flux of account ids
     */
    public Flux<UUID> fetchEnrolledAccountIds(final UUID cohortId) {
        return cohortEnrollmentGateway.findEnrolledAccountIds(cohortId);
    }

    /**
     * Find all the accounts that were ever enrolled to a cohort
     *
     * @param cohortId the cohort to fetch the enrolments for
     * @return a flux of historical cohort enrollment
     */
    public Flux<HistoricalCohortEnrollment> fetchHistoricalEnrollments(final UUID cohortId) {
        return cohortEnrollmentGateway.findHistoricalCohortEnrollments(cohortId);
    }

    /**
     * Build a cohort enrollment payload for a cohort enrollment and relative account
     * @param cohortEnrollment the cohort enrollment to map
     * @param account the account related to the cohort enrollment to fetch the info for
     * @return a {@link Mono} of {@link CohortEnrollmentPayload}
     */
    private Mono<CohortEnrollmentPayload> getCohortEnrollmentPayload(@Nonnull final CohortEnrollment cohortEnrollment,
                                                                     @Nonnull final Mono<Account> account) {
        return Mono.zip(Mono.just(cohortEnrollment), account.flatMap(accountService::getPartialAccountAdapter))
                .map(tuple2 ->{
                    AccountAdapter accountAdapter = tuple2.getT2();
                    return CohortEnrollmentPayload.from(tuple2.getT1(),
                            accountAdapter.getIdentityAttributes(), accountAdapter.getAvatars().get(0));
                });
    }

    /**
     * Find a cohort enrollment for the account for the given cohort
     * @param accountId the account id
     * @param cohortId the cohort id
     * @return a mono with CohortEnrollmemt or empty mono if the account is not enrolled to the cohort
     */
    public Mono<CohortEnrollment> getAccountEnrollment(final UUID accountId, final UUID cohortId) {
        affirmArgument(accountId != null, "missing accountId");

        return cohortEnrollmentGateway.findAccountEnrollment(accountId, cohortId);
    }

    /**
     * Find an historical account enrollment
     *
     * @param cohortId the cohort id
     * @param accountId the account id
     * @return a mono of historical account enrollment
     */
    public Mono<HistoricalCohortEnrollment> getHistoricalAccountEnrollment(final UUID cohortId, final UUID accountId) {
        affirmArgument(accountId != null, "accountId is required");
        return cohortEnrollmentGateway.findHistoricalAccountEnrollment(cohortId, accountId);
    }

    /**
     * Find the history of enrollments for this account in the given cohort; it is not intended to check
     * for enrollment validity.
     *
     * @param accountId the account id
     * @param cohortId the cohort id
     * @return a @{link Flux} of @{link CohortEnrollment} objects, or empty if none.
     */
    public Flux<CohortEnrollment> getAccountEnrollmentHistory(final UUID accountId, final UUID cohortId) {
        affirmArgument(accountId != null, "missing accountId");
        affirmArgument(cohortId != null, "missing cohortId");

        return cohortEnrollmentGateway.findEnrollmentHistory(accountId, cohortId);
    }
}
