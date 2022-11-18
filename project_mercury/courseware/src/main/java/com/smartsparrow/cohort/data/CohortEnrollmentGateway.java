package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortEnrollmentGateway {

    private static final Logger log = LoggerFactory.getLogger(CohortEnrollmentGateway.class);

    private final Session session;

    private final EnrollmentByAccountMaterializer enrollmentByAccountMaterializer;
    private final EnrollmentByAccountMutator enrollmentByAccountMutator;
    private final EnrollmentByCohortMaterializer enrollmentByCohortMaterializer;
    private final EnrollmentByCohortMutator enrollmentByCohortMutator;
    private final CohortEnrollmentStatusMutator cohortEnrollmentStatusMutator;
    private final CohortEnrollmentStatusMaterializer cohortEnrollmentStatusMaterializer;
    private final HistoricalEnrollmentByCohortMaterializer historicalEnrollmentByCohortMaterializer;
    private final HistoricalEnrollmentByCohortMutator historicalEnrollmentByCohortMutator;

    @Inject
    public CohortEnrollmentGateway(final Session session,
                                   final EnrollmentByAccountMaterializer enrollmentByAccountMaterializer,
                                   final EnrollmentByAccountMutator enrollmentByAccountMutator,
                                   final EnrollmentByCohortMaterializer enrollmentByCohortMaterializer,
                                   final EnrollmentByCohortMutator enrollmentByCohortMutator,
                                   final CohortEnrollmentStatusMutator cohortEnrollmentStatusMutator,
                                   final CohortEnrollmentStatusMaterializer cohortEnrollmentStatusMaterializer,
                                   final HistoricalEnrollmentByCohortMaterializer historicalEnrollmentByCohortMaterializer,
                                   final HistoricalEnrollmentByCohortMutator historicalEnrollmentByCohortMutator) {
        this.session = session;
        this.enrollmentByAccountMaterializer = enrollmentByAccountMaterializer;
        this.enrollmentByAccountMutator = enrollmentByAccountMutator;
        this.enrollmentByCohortMaterializer = enrollmentByCohortMaterializer;
        this.enrollmentByCohortMutator = enrollmentByCohortMutator;
        this.cohortEnrollmentStatusMutator = cohortEnrollmentStatusMutator;
        this.cohortEnrollmentStatusMaterializer = cohortEnrollmentStatusMaterializer;
        this.historicalEnrollmentByCohortMaterializer = historicalEnrollmentByCohortMaterializer;
        this.historicalEnrollmentByCohortMutator = historicalEnrollmentByCohortMutator;
    }

    /**
     * Save a cohort enrollment. This method consistently save a cohort enrollment in enrollment_by_cohort and
     * enrollment_by_account.
     *
     * @param cohortEnrollment the cohort enrollment to save
     * @return a {@link Flux} of {@link Void}
     */
    @Trace(async = true)
    public Flux<Void> persist(CohortEnrollment cohortEnrollment) {
        return Mutators.execute(session, Flux.just(
                enrollmentByAccountMutator.upsert(cohortEnrollment),
                enrollmentByCohortMutator.upsert(cohortEnrollment),
                cohortEnrollmentStatusMutator.upsert(cohortEnrollment),
                historicalEnrollmentByCohortMutator.upsert(cohortEnrollment)
        ))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort enrollment %s", cohortEnrollment),
                              throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a cohort enrollment. This method consistently delete a cohort enrollment from enrollment_by_cohort and
     * enrollment_by_account.
     *
     * @param cohortEnrollment the cohort enrollment to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(CohortEnrollment cohortEnrollment) {
        return Mutators.execute(session, Flux.just(
                enrollmentByCohortMutator.delete(cohortEnrollment),
                enrollmentByAccountMutator.delete(cohortEnrollment)
        )).doOnError(throwable -> {
            log.error(String.format("error while deleting cohort enrollment %s", cohortEnrollment),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist a cohort enrollment to the status table. This method should be used only when the intention is to persist
     * a cohort enrollment with {@link EnrollmentType#UNENROLLED}. For all other types of enrollment
     * {@link CohortEnrollmentGateway#persist(CohortEnrollment)} should be used instead.
     *
     * @param cohortEnrollment the cohort enrollment to persist to the status table
     * @return a flux of void
     */
    public Flux<Void> persistCohortEnrollmentStatus(CohortEnrollment cohortEnrollment) {
        return Mutators.execute(session, Flux.just(
                cohortEnrollmentStatusMutator.upsert(cohortEnrollment)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving cohort enrollment %s", cohortEnrollment),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find all enrollments for a cohort.
     *
     * @param cohortId the cohort to fetch the enrollments for
     * @return a {@link Flux} of {@link CohortEnrollment}
     */
    public Flux<CohortEnrollment> findCohortEnrollments(UUID cohortId) {
        return ResultSets.query(session, enrollmentByCohortMaterializer.findEnrollments(cohortId))
                .flatMapIterable(row->row)
                .map(enrollmentByCohortMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort enrollments for cohort %s",
                            cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the accounts that are currently enrolled to a cohort
     *
     * @param cohortId the cohort to fetch the enrolled accounts for
     * @return a flux of account ids
     */
    public Flux<UUID> findEnrolledAccountIds(final UUID cohortId) {
        return ResultSets.query(session, enrollmentByCohortMaterializer.findEnrolledAccountIds(cohortId))
                .flatMapIterable(row->row)
                .map(row -> row.getUUID("account_id"))
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort enrollments for cohort %s",
                            cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all the accounts that were ever enrolled to a cohort
     *
     * @param cohortId the cohort to fetch the enrollments for
     * @return a flux of historical cohort enrolments
     */
    public Flux<HistoricalCohortEnrollment> findHistoricalCohortEnrollments(final UUID cohortId) {
        return ResultSets.query(session, historicalEnrollmentByCohortMaterializer.findEnrollments(cohortId))
                .flatMapIterable(row->row)
                .map(historicalEnrollmentByCohortMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching historical cohort enrollments for cohort %s",
                            cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find an historical account enrollment to a cohort
     *
     * @param cohortId the cohort to find the enrollment for
     * @param accountId the account to find
     * @return a mono of historical cohort enrollment
     */
    public Mono<HistoricalCohortEnrollment> findHistoricalAccountEnrollment(final UUID cohortId, final UUID accountId) {
        return ResultSets.query(session, historicalEnrollmentByCohortMaterializer.findEnrollment(cohortId, accountId))
                .flatMapIterable(row->row)
                .map(historicalEnrollmentByCohortMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching historical cohort enrollment for cohort %s",
                            cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all enrollments for an account.
     *
     * @param accountId the account to search the enrollments for
     * @return a {@link Flux} of {@link CohortEnrollment}
     */
    public Flux<CohortEnrollment> findAccountEnrollments(UUID accountId) {
        return ResultSets.query(session, enrollmentByAccountMaterializer.findEnrollments(accountId))
                .flatMapIterable(row->row)
                .map(enrollmentByAccountMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort enrollments for account %s",
                            accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find cohort enrollment for an account
     *
     * @param accountId the account to search the enrollment for
     * @param cohortId  the cohort to search the enrollment for
     * @return a {@link Mono} of {@link CohortEnrollment}, or empty mono if no enrollment to the cohort for the account
     */
    public Mono<CohortEnrollment> findAccountEnrollment(UUID accountId, UUID cohortId) {
        return ResultSets.query(session, enrollmentByAccountMaterializer.findEnrollment(accountId, cohortId))
                .flatMapIterable(row -> row)
                .map(enrollmentByAccountMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the enrollment history for this user on this cohort, not intended to be used to validate an enrollment
     *
     * @param accountId the account to find
     * @param cohortId the cohort context
     * @return a {@link Flux} of {@link CohortEnrollment} containing all the enrollments for an account, or empty if none exist
     */
    public Flux<CohortEnrollment> findEnrollmentHistory(final UUID accountId, final UUID cohortId) {
        return ResultSets.query(session, cohortEnrollmentStatusMaterializer.findEnrollments(cohortId, accountId))
                .flatMapIterable(row -> row)
                .map(cohortEnrollmentStatusMaterializer::fromRow);
    }
}
