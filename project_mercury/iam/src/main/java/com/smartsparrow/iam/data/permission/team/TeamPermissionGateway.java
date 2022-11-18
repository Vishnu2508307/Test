package com.smartsparrow.iam.data.permission.team;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Manages the database operations to the IAM team permissions
@Singleton
public class TeamPermissionGateway {

    private final Session session;

    private final TeamPermissionByAccountMaterializer teamPermissionByAccountMaterializer;
    private final TeamPermissionByAccountMutator teamPermissionByAccountMutator;

    @Inject
    public TeamPermissionGateway(Session session,
                                 TeamPermissionByAccountMutator teamPermissionByAccountMutator,
                                 TeamPermissionByAccountMaterializer teamPermissionByAccountMaterializer) {
        this.session = session;
        this.teamPermissionByAccountMaterializer = teamPermissionByAccountMaterializer;
        this.teamPermissionByAccountMutator = teamPermissionByAccountMutator;
    }


    /**
     * Persist a team permission to cassandra
     *
     * @param teamPermission {@link TeamPermission}
     */
    @Trace(async = true)
    public Flux<Void> persist(final TeamPermission teamPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(teamPermissionByAccountMutator, teamPermission);
        return Mutators.execute(session, stmt)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a team permission from cassandra
     *
     * @param teamPermission {@link TeamPermission}
     */
    public Flux<Void> delete(final TeamPermission teamPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(teamPermissionByAccountMutator, teamPermission);
        return Mutators.execute(session, stmt);
    }

    /**
     * @param accountId - unique identifier for an account
     * @param teamId    - unique identifier for a team
     * @return {@link Mono<TeamPermission>}
     */
    public Mono<TeamPermission> findPermission(final UUID accountId, final UUID teamId) {
        return ResultSets.query(session,
                teamPermissionByAccountMaterializer.fetchPermission(accountId, teamId))
                .flatMapIterable(row -> row)
                .map(teamPermissionByAccountMaterializer::fromRow)
                .singleOrEmpty();
    }
}
