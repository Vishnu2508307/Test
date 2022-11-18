package com.smartsparrow.iam.data.permission.workspace;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ProjectPermissionGateway {

    private static final Logger log = LoggerFactory.getLogger(ProjectPermissionGateway.class);

    private final Session session;

    private final AccountProjectPermissionMaterializer accountProjectPermissionMaterializer;
    private final AccountProjectPermissionMutator accountProjectPermissionMutator;
    private final TeamProjectPermissionMaterializer teamProjectPermissionMaterializer;
    private final TeamProjectPermissionMutator teamProjectPermissionMutator;

    @Inject
    public ProjectPermissionGateway(final Session session,
                                    final AccountProjectPermissionMaterializer accountProjectPermissionMaterializer,
                                    final AccountProjectPermissionMutator accountProjectPermissionMutator,
                                    final TeamProjectPermissionMaterializer teamProjectPermissionMaterializer,
                                    final TeamProjectPermissionMutator teamProjectPermissionMutator) {
        this.session = session;
        this.accountProjectPermissionMaterializer = accountProjectPermissionMaterializer;
        this.accountProjectPermissionMutator = accountProjectPermissionMutator;
        this.teamProjectPermissionMaterializer = teamProjectPermissionMaterializer;
        this.teamProjectPermissionMutator = teamProjectPermissionMutator;
    }

    /**
     * Save a project permission level for an account
     *
     * @param accountProjectPermission the permission to save
     * @return a flux of void
     */
    public Flux<Void> persist(final AccountProjectPermission accountProjectPermission) {
        return Mutators.execute(session, Flux.just(
                accountProjectPermissionMutator.upsert(accountProjectPermission)
        )).doOnError(throwable -> {
            log.error(String.format("Error persisting the account project permission [%s]", accountProjectPermission.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete a project permission for an account
     *
     * @param accountProjectPermission the permission to delete
     * @return a flux of void
     */
    public Flux<Void> delete(final AccountProjectPermission accountProjectPermission) {
        return Mutators.execute(session, Flux.just(
                accountProjectPermissionMutator.delete(accountProjectPermission)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting the account project permission [%s]", accountProjectPermission.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the permission level for an account over a given project
     *
     * @param accountId the account to fetch the permission for
     * @param projectId the project the permission refers to
     * @return a mono of
     */
    @Trace(async = true)
    public Mono<PermissionLevel> findAccountPermission(final UUID accountId, final UUID projectId) {
        return ResultSets.query(session,
                accountProjectPermissionMaterializer.fetchPermissionLevel(accountId, projectId))
                .flatMapIterable(row -> row)
                .map(accountProjectPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save a project permission level for a team
     *
     * @param teamProjectPermission the permission to save
     * @return a flux of void
     */
    public Flux<Void> persist(final TeamProjectPermission teamProjectPermission) {
        return Mutators.execute(session, Flux.just(
                teamProjectPermissionMutator.upsert(teamProjectPermission)
        )).doOnError(throwable -> {
            log.error(String.format("Error persisting the team project permission [%s]", teamProjectPermission.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Delete a project permission for a team
     *
     * @param teamProjectPermission the permission to delete
     * @return a flux of void
     */
    public Flux<Void> delete(final TeamProjectPermission teamProjectPermission) {
        return Mutators.execute(session, Flux.just(
                teamProjectPermissionMutator.delete(teamProjectPermission)
        )).doOnError(throwable -> {
            log.error(String.format("Error deleting the team project permission [%s]", teamProjectPermission.toString()),
                    throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find the permission level for a team over a given project
     *
     * @param teamId the team to fetch the permission for
     * @param projectId the project the permission refers to
     * @return a mono of
     */
    @Trace(async = true)
    public Mono<PermissionLevel> findTeamPermission(final UUID teamId, final UUID projectId) {
        return ResultSets.query(session,
                teamProjectPermissionMaterializer.fetchPermissionLevel(teamId, projectId))
                .flatMapIterable(row -> row)
                .map(teamProjectPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
