package com.smartsparrow.iam.data.team;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class TeamGateway {

    private static final Logger log = LoggerFactory.getLogger(TeamGateway.class);

    private final Session session;
    private final AccountByTeamMaterializer accountByTeamMaterializer;
    private final AccountByTeamMutator accountByTeamMutator;
    private final TeamByAccountMaterializer teamByAccountMaterializer;
    private final TeamByAccountMutator teamByAccountMutator;
    private final TeamSummaryMaterializer teamSummaryMaterializer;
    private final TeamSummaryMutator teamSummaryMutator;
    private final TeamBySubscriptionMaterializer teamBySubscriptionMaterializer;
    private final TeamBySubscriptionMutator teamBySubscriptionMutator;

    @Inject
    public TeamGateway(Session session,
                       AccountByTeamMaterializer accountByTeamMaterializer,
                       AccountByTeamMutator accountByTeamMutator,
                       TeamByAccountMaterializer teamByAccountMaterializer,
                       TeamByAccountMutator teamByAccountMutator,
                       TeamSummaryMaterializer teamSummaryMaterializer,
                       TeamSummaryMutator teamSummaryMutator,
                       TeamBySubscriptionMaterializer teamBySubscriptionMaterializer,
                       TeamBySubscriptionMutator teamBySubscriptionMutator) {
        this.session = session;
        this.accountByTeamMaterializer = accountByTeamMaterializer;
        this.accountByTeamMutator = accountByTeamMutator;
        this.teamByAccountMaterializer = teamByAccountMaterializer;
        this.teamByAccountMutator = teamByAccountMutator;
        this.teamSummaryMaterializer = teamSummaryMaterializer;
        this.teamSummaryMutator = teamSummaryMutator;
        this.teamBySubscriptionMaterializer = teamBySubscriptionMaterializer;
        this.teamBySubscriptionMutator = teamBySubscriptionMutator;
    }


    /**
     * Persist a team summary
     *
     * @param teamSummary {@link TeamSummary}
     */
    @Trace(async = true)
    public Mono<Void> persist(final TeamSummary teamSummary) {
        return Mutators.execute(session,
                                Flux.just(teamSummaryMutator.upsert(teamSummary)))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist an accountTeamCollaborator
     *
     * @param accountTeamCollaborator {@link AccountTeamCollaborator}
     */
    @Trace(async = true)
    public Mono<Void> persist(final AccountTeamCollaborator accountTeamCollaborator) {
        return Mutators.execute(session,
                                Flux.just(accountByTeamMutator.upsert(accountTeamCollaborator)))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a teamAccountMapping
     *
     * @param teamAccount {@link TeamAccount}
     */
    @Trace(async = true)
    public Mono<Void> persist(final TeamAccount teamAccount) {
        return Mutators.execute(session,
                                Flux.just(teamByAccountMutator.upsert(teamAccount)))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the mapping of a team and a subscription
     *
     * @param teamBySubscription {@link TeamBySubscription}
     */
    public Mono<Void> persist(final TeamBySubscription teamBySubscription) {
        return Mutators.execute(session,
                Flux.just(teamBySubscriptionMutator.upsert(teamBySubscription)))
                .singleOrEmpty();
    }

    /**
     * Deletes a teamSummary
     *
     * @param teamSummary {@link TeamSummary}
     */
    public Flux<Void> delete(final TeamSummary teamSummary) {
        Flux<? extends Statement> stmt = Mutators.delete(teamSummaryMutator, teamSummary);
        return Mutators.execute(session, stmt);
    }

    /**
     * Deletes a accountTeamCollaborator
     *
     * @param accountTeamCollaborator {@link AccountTeamCollaborator}
     */
    @Trace(async = true)
    public Flux<Void> delete(final AccountTeamCollaborator accountTeamCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(accountByTeamMutator, accountTeamCollaborator);
        return Mutators.execute(session, stmt)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Deletes a team Account mapping
     *
     * @param teamAccount {@link TeamAccount}
     */
    @Trace(async = true)
    public Flux<Void> delete(final TeamAccount teamAccount) {
        Flux<? extends Statement> stmt = Mutators.delete(teamByAccountMutator, teamAccount);
        return Mutators.execute(session, stmt)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Deletes a team Account mapping
     *
     * @param teamBySubscription {@link TeamBySubscription}
     */
    public Flux<Void> delete(final TeamBySubscription teamBySubscription) {
        Flux<? extends Statement> stmt = Mutators.delete(teamBySubscriptionMutator, teamBySubscription);
        return Mutators.execute(session, stmt);
    }

    /**
     * Find a team by teamId
     *
     * @param teamId
     * @return {@link Mono<TeamSummary>}
     */
    @Trace(async = true)
    public Mono<TeamSummary> findTeam(final UUID teamId) {
        return ResultSets.query(session,
                teamSummaryMaterializer.fetchTeamSummaryByTeam(teamId))
                .flatMapIterable(row -> row)
                .map(teamSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(e -> {
                    log.error(String.format("error while fetching team summary with id %s", teamId), e);
                    throw Exceptions.propagate(e);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find teams for a given subscription
     *
     * @param subscriptionId
     * @return {@link TeamBySubscription}
     */
    @Trace(async = true)
    public Flux<TeamBySubscription> findTeamsForSubscription(final UUID subscriptionId) {
        return ResultSets.query(session,
                teamBySubscriptionMaterializer.fetchBySubscriptionId(subscriptionId))
                .flatMapIterable(row -> row)
                .map(teamBySubscriptionMaterializer::fromRow)
                .doOnError(e -> {
                    log.error(String
                            .format("error while fetching teams with subscription_id %s",
                                    subscriptionId), e);
                    throw Exceptions.propagate(e);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find account collaborators for a team
     *
     * @param teamId
     * @return {@link AccountTeamCollaborator}
     */
    @Trace(async = true)
    public Flux<AccountTeamCollaborator> findAccountCollaborators(final UUID teamId) {
        return ResultSets.query(session,
                accountByTeamMaterializer.fetchByTeamId(teamId))
                .flatMapIterable(row -> row)
                .map(accountByTeamMaterializer::fromRow)
                .doOnError(e -> {
                    log.error(String
                            .format("error while fetching collaborators with team_id %s",
                                    teamId), e);
                    throw Exceptions.propagate(e);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find associated teams for an account
     *
     * @param accountId
     * @return {@link TeamAccount}
     */
    @Trace(async = true)
    public Flux<TeamAccount> findTeamsForAccount(final UUID accountId) {
        return ResultSets.query(session,
                teamByAccountMaterializer.fetchByAccountId(accountId))
                .flatMapIterable(row -> row)
                .map(teamByAccountMaterializer::fromRow)
                .doOnError(e -> {
                    log.error(String
                            .format("error while fetching teams for account_id %s",
                                    accountId), e);
                    throw Exceptions.propagate(e);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
