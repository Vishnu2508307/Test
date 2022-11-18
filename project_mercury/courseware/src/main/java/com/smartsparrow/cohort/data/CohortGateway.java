package com.smartsparrow.cohort.data;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortGateway {

    private static final Logger log = LoggerFactory.getLogger(CohortGateway.class);

    private final Session session;

    private final CohortSummaryMaterializer cohortSummaryMaterializer;
    private final CohortSummaryMutator cohortSummaryMutator;
    private final AccountCohortCollaboratorMaterializer accountCohortCollaboratorMaterializer;
    private final AccountCohortCollaboratorMutator accountCohortCollaboratorMutator;
    private final CohortByAccountMaterializer cohortByAccountMaterializer;
    private final CohortByAccountMutator cohortByAccountMutator;
    private final TeamCohortCollaboratorMaterializer teamCohortCollaboratorMaterializer;
    private final TeamCohortCollaboratorMutator teamCohortCollaboratorMutator;
    private final CohortByTeamMaterializer cohortByTeamMaterializer;
    private final CohortByTeamMutator cohortByTeamMutator;
    private final CohortByProductMaterializer cohortByProductMaterializer;
    private final CohortByProductMutator cohortByProductMutator;
    private final CohortByLmsCourseMaterializer cohortByLmsCourseMaterializer;
    private final CohortByLmsCourseMutator cohortByLmsCourseMutator;
    private final InstanceByTemplateMaterializer instanceByTemplateMaterializer;
    private final InstanceByTemplateMutator instanceByTemplateMutator;

    @Inject
    public CohortGateway(Session session,
                         CohortSummaryMaterializer cohortSummaryMaterializer,
                         CohortSummaryMutator cohortSummaryMutator,
                         AccountCohortCollaboratorMaterializer accountCohortCollaboratorMaterializer,
                         AccountCohortCollaboratorMutator accountCohortCollaboratorMutator,
                         CohortByAccountMaterializer cohortByAccountMaterializer,
                         CohortByAccountMutator cohortByAccountMutator,
                         TeamCohortCollaboratorMaterializer teamCohortCollaboratorMaterializer,
                         TeamCohortCollaboratorMutator teamCohortCollaboratorMutator,
                         CohortByTeamMaterializer cohortByTeamMaterializer,
                         CohortByTeamMutator cohortByTeamMutator,
                         CohortByProductMaterializer cohortByProductMaterializer,
                         CohortByProductMutator cohortByProductMutator,
                         CohortByLmsCourseMaterializer cohortByLmsCourseMaterializer,
                         CohortByLmsCourseMutator cohortByLmsCourseMutator,
                         InstanceByTemplateMaterializer instanceByTemplateMaterializer,
                         InstanceByTemplateMutator instanceByTemplateMutator) {
        this.session = session;
        this.cohortSummaryMaterializer = cohortSummaryMaterializer;
        this.cohortSummaryMutator = cohortSummaryMutator;
        this.accountCohortCollaboratorMaterializer = accountCohortCollaboratorMaterializer;
        this.accountCohortCollaboratorMutator = accountCohortCollaboratorMutator;
        this.cohortByAccountMaterializer = cohortByAccountMaterializer;
        this.cohortByAccountMutator = cohortByAccountMutator;
        this.teamCohortCollaboratorMaterializer = teamCohortCollaboratorMaterializer;
        this.teamCohortCollaboratorMutator = teamCohortCollaboratorMutator;
        this.cohortByTeamMaterializer = cohortByTeamMaterializer;
        this.cohortByTeamMutator = cohortByTeamMutator;
        this.cohortByProductMaterializer = cohortByProductMaterializer;
        this.cohortByProductMutator = cohortByProductMutator;
        this.cohortByLmsCourseMaterializer = cohortByLmsCourseMaterializer;
        this.cohortByLmsCourseMutator = cohortByLmsCourseMutator;
        this.instanceByTemplateMaterializer = instanceByTemplateMaterializer;
        this.instanceByTemplateMutator = instanceByTemplateMutator;
    }

    /**
     * Save an account collaborating over a cohort
     *
     * @param accountCohortCollaborator the collaborator account
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(AccountCohortCollaborator accountCohortCollaborator) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(accountCohortCollaboratorMutator), accountCohortCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort permission %s",
                            accountCohortCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a team collaborating over a cohort
     *
     * @param teamCohortCollaborator the collaborating team
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(TeamCohortCollaborator teamCohortCollaborator) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(teamCohortCollaboratorMutator), teamCohortCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort permission %s",
                            teamCohortCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a cohort summary
     *
     * @param cohortSummary the {@link CohortSummary} to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(CohortSummary cohortSummary) {
        return Mutators.execute(session, Mutators.upsert(cohortSummaryMutator, cohortSummary))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort summary %s", cohortSummary), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Update cohort summary
     *
     * @param cohortSummary cohort info to save
     */
    public Flux<Void> update(CohortSummary cohortSummary) {
        return Mutators.execute(session, Flux.just(cohortSummaryMutator.update(cohortSummary)))
                .doOnError(throwable -> {
                    log.error(String.format("error while updating cohort summary %s", cohortSummary), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete an account from list of cohort collaborators. This only requires accountId and cohortId to be defined when deleting.
     *
     * @param accountCohortCollaborator the collaborator to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(AccountCohortCollaborator accountCohortCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(accountCohortCollaboratorMutator), accountCohortCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting cohort permission %s",
                            accountCohortCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a team from list of cohort collaborators. This only requires teamId and cohortId to be defined when deleting.
     *
     * @param teamCohortCollaborator the collaborator to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(TeamCohortCollaborator teamCohortCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(teamCohortCollaboratorMutator), teamCohortCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting cohort permission %s",
                            teamCohortCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a cohort access for an account
     *
     * @param cohortAccount the account cohort access to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(CohortAccount cohortAccount) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(cohortByAccountMutator), cohortAccount);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort account %s",
                            cohortAccount), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a cohort access for a team
     *
     * @param cohortId the cohort an access to save for
     * @param teamId   the team an access to save for
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(UUID cohortId, UUID teamId) {
        Flux<? extends Statement> stmt = Flux.just(cohortByTeamMutator.upsert(cohortId, teamId));
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving access for cohort %s for team %s",
                            cohortId, teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a cohort access for an account.
     *
     * @param cohortAccount the account cohort access to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(CohortAccount cohortAccount) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(cohortByAccountMutator), cohortAccount);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting cohort account %s",
                            cohortAccount), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a cohort access for a team.
     *
     * @param cohortId the cohort an access to delete for
     * @param teamId   the team an access to delete for
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(UUID cohortId, UUID teamId) {
        Flux<? extends Statement> stmt = Flux.just(cohortByTeamMutator.delete(cohortId, teamId));
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting access for cohort %s for team %s",
                            cohortId, teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all account collaborators on a cohort
     *
     * @param cohortId the cohort to search the collaborators for
     * @return a {@link Flux} of {@link AccountCohortCollaborator}
     */
    public Flux<AccountCohortCollaborator> findAccountCollaborators(UUID cohortId) {
        return ResultSets.query(session, accountCohortCollaboratorMaterializer.fetchAccounts(cohortId))
                .flatMapIterable(row -> row)
                .map(accountCohortCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching collaborators for cohort %s", cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all team collaborators on a cohort
     *
     * @param cohortId the cohort to search the collaborators for
     * @return a {@link Flux} of {@link TeamCohortCollaborator}
     */
    public Flux<TeamCohortCollaborator> findTeamCollaborators(UUID cohortId) {
        return ResultSets.query(session, teamCohortCollaboratorMaterializer.findTeams(cohortId))
                .flatMapIterable(row -> row)
                .map(teamCohortCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching collaborators for cohort %s", cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a cohort summary by id.
     *
     * @param id the cohort {@link UUID}
     * @return a {@link Mono} of {@link CohortSummary}
     */
    @Trace(async = true)
    public Mono<CohortSummary> findCohortSummary(UUID id) {
        return ResultSets.query(session, cohortSummaryMaterializer.fetchById(id))
                .flatMapIterable(row -> row)
                .map(cohortSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort summary with id %s", id), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the cohorts an account has access to
     *
     * @param accountId the account to search the cohorts for
     * @return a {@link Flux} of cohort ids
     */
    public Flux<UUID> findCohortsByAccount(UUID accountId) {
        return ResultSets.query(session, cohortByAccountMaterializer.fetchCohorts(accountId))
                .flatMapIterable(row -> row)
                .map(cohortByAccountMaterializer::fromRow)
                .map(CohortAccount::getCohortId)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohorts for account %s", accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the cohorts a team has access to
     *
     * @param teamId the team to search the cohorts for
     * @return a {@link Flux} of cohort ids
     */
    public Flux<UUID> findCohortsByTeam(UUID teamId) {
        return ResultSets.query(session, cohortByTeamMaterializer.findCohorts(teamId))
                .flatMapIterable(row -> row)
                .map(cohortByTeamMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohorts for team %s", teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Set a cohort finished date
     *
     * @param cohortId     the cohort id to set the finished date for
     * @param finishedDate the finished date to set to the cohort
     */
    public Flux<Void> setFinish(UUID cohortId, UUID finishedDate) {
        return Mutators.execute(session, Flux.just(cohortSummaryMutator.setFinished(cohortId, finishedDate)));
    }

    /**
     * Persist product id to cohort id
     *
     * @param productId the product id to persist
     * @param cohortId the cohort id to persist
     * @return a flux of void
     */
    public Flux<Void> persistIdByProduct(String productId, UUID cohortId) {
        return Mutators.execute(session, Flux.just(
                cohortByProductMutator.upsert(productId, cohortId)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the cohort id for a given product id.
     *
     * @param productId the product id to search the cohort id for
     * @return cohort id mono, or empty
     */
    @Trace(async = true)
    public Mono<UUID> findIdByProduct(String productId) {
        return ResultSets.query(session, cohortByProductMaterializer.findDeployment(productId))
                .flatMapIterable(row -> row)
                .map(cohortByProductMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist LMS course id to cohort id
     *
     * @param lmsCourseId the LMS course id to persist
     * @param cohortId the cohort id to persist
     * @return a flux of void
     */
    public Flux<Void> persistIdByLmsCourse(String lmsCourseId, UUID cohortId) {
        return Mutators.execute(session, Flux.just(
                cohortByLmsCourseMutator.upsert(lmsCourseId, cohortId)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the cohort id for a given LMS course id.
     *
     * @param lmsCourseId the LMS course id to search the cohort id for
     * @return cohort id mono, or empty
     */
    @Trace(async = true)
    public Mono<UUID> findIdByLmsCourse(String lmsCourseId) {
        return ResultSets.query(session, cohortByLmsCourseMaterializer.findCohort(lmsCourseId))
                .flatMapIterable(row -> row)
                .map(cohortByLmsCourseMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save a cohort instance for a cohort template
     *
     * @param templateCohortId  the cohort template id
     * @param instanceCohortId  the cohort instance id
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persistCohortInstanceByTemplate(UUID templateCohortId, UUID instanceCohortId) {
        Flux<? extends Statement> stmt = Flux.just(instanceByTemplateMutator.upsert(templateCohortId, instanceCohortId));
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort instance %s for cohort template %s",
                                            instanceCohortId, templateCohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the cohort instance ids associated to this cohort template id
     *
     * @param templateCohortId the team to search the cohorts for
     * @return a {@link Flux} of cohort instance ids
     */
    public Flux<UUID> findCohortInstancesByTemplate(UUID templateCohortId) {
        return ResultSets.query(session, instanceByTemplateMaterializer.findCohortInstances(templateCohortId))
                .flatMapIterable(row -> row)
                .map(instanceByTemplateMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort instances for cohort template %s", templateCohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
