package com.smartsparrow.iam.data.permission.cohort;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortPermissionGateway {

    private static final Logger log = LoggerFactory.getLogger(CohortPermissionGateway.class);

    private final Session session;

    private final CohortAccountPermissionMutator cohortAccountPermissionMutator;
    private final CohortAccountPermissionMaterializer cohortAccountPermissionMaterializer;
    private final CohortTeamPermissionMutator cohortTeamPermissionMutator;
    private final CohortTeamPermissionMaterializer cohortTeamPermissionMaterializer;

    @Inject
    public CohortPermissionGateway(Session session,
                                   CohortAccountPermissionMutator cohortAccountPermissionMutator,
                                   CohortAccountPermissionMaterializer cohortAccountPermissionMaterializer,
                                   CohortTeamPermissionMutator cohortTeamPermissionMutator,
                                   CohortTeamPermissionMaterializer cohortTeamPermissionMaterializer) {
        this.session = session;
        this.cohortAccountPermissionMutator = cohortAccountPermissionMutator;
        this.cohortAccountPermissionMaterializer = cohortAccountPermissionMaterializer;
        this.cohortTeamPermissionMutator = cohortTeamPermissionMutator;
        this.cohortTeamPermissionMaterializer = cohortTeamPermissionMaterializer;
    }

    /**
     * Save an account cohort permission
     *
     * @param accountCohortPermission the permission to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(AccountCohortPermission accountCohortPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(cohortAccountPermissionMutator, accountCohortPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort permission on iam_global for %s",
                            accountCohortPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a team cohort permission
     *
     * @param teamCohortPermission the permission to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(TeamCohortPermission teamCohortPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(cohortTeamPermissionMutator, teamCohortPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort permission on iam_global for %s",
                            teamCohortPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete an account cohort permission. The {@link AccountCohortPermission#getPermissionLevel()} supplied by the
     * argument can be <code>null</code>. This only requires accountId and cohortId to be defined when deleting.
     *
     * @param accountCohortPermission the permission to delete
     */
    public Flux<Void> delete(AccountCohortPermission accountCohortPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(cohortAccountPermissionMutator, accountCohortPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting cohort permission on iam_global for %s",
                            accountCohortPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a team cohort permission. The {@link TeamCohortPermission#getPermissionLevel()} supplied by the
     * argument can be <code>null</code>. This only requires teamId and cohortId to be defined when deleting.
     *
     * @param teamCohortPermission the permission to delete
     */
    public Flux<Void> delete(TeamCohortPermission teamCohortPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(cohortTeamPermissionMutator, teamCohortPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting cohort permission on iam_global for %s",
                            teamCohortPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find the permission level for an account over a given cohort
     *
     * @param accountId the account to fetch the permission for
     * @param cohortId  the cohort entity the permission refers to
     * @return a {@link Mono} of {@link AccountCohortPermission}
     */
    public Mono<AccountCohortPermission> findAccountPermission(UUID accountId, UUID cohortId) {
        return ResultSets.query(session, cohortAccountPermissionMaterializer.fetchPermission(accountId, cohortId))
                .flatMapIterable(row -> row)
                .map(cohortAccountPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for account %s and cohort %s",
                            accountId, cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find the permission level for a team over a given cohort
     *
     * @param teamId the team to fetch the permission for
     * @param cohortId  the cohort entity the permission refers to
     * @return a {@link Mono} of {@link TeamCohortPermission}
     */
    public Mono<TeamCohortPermission> findTeamPermission(UUID teamId, UUID cohortId) {
        return ResultSets.query(session, cohortTeamPermissionMaterializer.fetchPermission(teamId, cohortId))
                .flatMapIterable(row -> row)
                .map(cohortTeamPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for team %s and cohort %s",
                            teamId, cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the cohorts an account has access to
     *
     * @param accountId the account id to search the cohort permissions for
     * @return a {@link Flux} of {@link AccountCohortPermission}
     */
    public Flux<AccountCohortPermission> findPermissions(UUID accountId) {
        return ResultSets.query(session, cohortAccountPermissionMaterializer.fetchPermission(accountId))
                .flatMapIterable(row -> row)
                .map(cohortAccountPermissionMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for account %s", accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }


}
