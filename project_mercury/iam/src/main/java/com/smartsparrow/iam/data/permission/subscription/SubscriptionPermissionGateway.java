package com.smartsparrow.iam.data.permission.subscription;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.data.SubscriptionAccount;
import com.smartsparrow.iam.data.SubscriptionAccountCollaborator;
import com.smartsparrow.iam.data.SubscriptionByAccountMaterializer;
import com.smartsparrow.iam.data.SubscriptionByAccountMutator;
import com.smartsparrow.iam.data.SubscriptionByTeamMaterializer;
import com.smartsparrow.iam.data.SubscriptionByTeamMutator;
import com.smartsparrow.iam.data.SubscriptionTeam;
import com.smartsparrow.iam.data.SubscriptionTeamCollaborator;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class SubscriptionPermissionGateway {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPermissionGateway.class);

    private final Session session;

    private final SubscriptionPermissionByAccountMaterializer subscriptionPermissionByAccountMaterializer;
    private final SubscriptionPermissionByAccountMutator subscriptionPermissionByAccountMutator;
    private final TeamSubscriptionPermissionMaterializer teamSubscriptionPermissionMaterializer;
    private final TeamSubscriptionPermissionMutator teamSubscriptionPermissionMutator;
    private final SubscriptionByAccountMaterializer subscriptionByAccountMaterializer;
    private final SubscriptionByAccountMutator subscriptionByAccountMutator;
    private final AccountSubscriptionCollaboratorMaterializer accountSubscriptionCollaboratorMaterializer;
    private final AccountSubscriptionCollaboratorMutator accountSubscriptionCollaboratorMutator;
    private final TeamSubscriptionCollaboratorMaterializer teamSubscriptionCollaboratorMaterializer;
    private final TeamSubscriptionCollaboratorMutator teamSubscriptionCollaboratorMutator;
    private final SubscriptionByTeamMutator subscriptionByTeamMutator;
    private final SubscriptionByTeamMaterializer subscriptionByTeamMaterializer;

    @Inject
    public SubscriptionPermissionGateway(Session session,
                                         SubscriptionPermissionByAccountMaterializer subscriptionPermissionByAccountMaterializer,
                                         SubscriptionPermissionByAccountMutator subscriptionPermissionByAccountMutator,
                                         TeamSubscriptionPermissionMaterializer teamSubscriptionPermissionMaterializer,
                                         TeamSubscriptionPermissionMutator teamSubscriptionPermissionMutator,
                                         SubscriptionByAccountMaterializer subscriptionByAccountMaterializer,
                                         SubscriptionByAccountMutator subscriptionByAccountMutator,
                                         AccountSubscriptionCollaboratorMaterializer accountSubscriptionCollaboratorMaterializer,
                                         AccountSubscriptionCollaboratorMutator accountSubscriptionCollaboratorMutator,
                                         TeamSubscriptionCollaboratorMaterializer teamSubscriptionCollaboratorMaterializer,
                                         TeamSubscriptionCollaboratorMutator teamSubscriptionCollaboratorMutator,
                                         SubscriptionByTeamMutator subscriptionByTeamMutator,
                                         SubscriptionByTeamMaterializer subscriptionByTeamMaterializer) {
        this.session = session;
        this.subscriptionPermissionByAccountMaterializer = subscriptionPermissionByAccountMaterializer;
        this.subscriptionPermissionByAccountMutator = subscriptionPermissionByAccountMutator;
        this.teamSubscriptionPermissionMaterializer = teamSubscriptionPermissionMaterializer;
        this.teamSubscriptionPermissionMutator = teamSubscriptionPermissionMutator;
        this.subscriptionByAccountMaterializer = subscriptionByAccountMaterializer;
        this.subscriptionByAccountMutator = subscriptionByAccountMutator;
        this.accountSubscriptionCollaboratorMaterializer = accountSubscriptionCollaboratorMaterializer;
        this.accountSubscriptionCollaboratorMutator = accountSubscriptionCollaboratorMutator;
        this.teamSubscriptionCollaboratorMaterializer = teamSubscriptionCollaboratorMaterializer;
        this.teamSubscriptionCollaboratorMutator = teamSubscriptionCollaboratorMutator;
        this.subscriptionByTeamMutator = subscriptionByTeamMutator;
        this.subscriptionByTeamMaterializer = subscriptionByTeamMaterializer;
    }


    /**
     * Save an account subscription permission
     *
     * @param permission the {@link AccountSubscriptionPermission} object to be saved
     * @return a {@link Flux} of {@link Void}
     */
    @Trace(async = true)
    public Flux<Void> saveAccountPermission(AccountSubscriptionPermission permission) {
        return Mutators.execute(session, Flux.just(
                accountSubscriptionCollaboratorMutator.upsert(new SubscriptionAccountCollaborator()
                        .setAccountId(permission.getAccountId())
                        .setPermissionLevel(permission.getPermissionLevel())
                        .setSubscriptionId(permission.getSubscriptionId())),
                subscriptionPermissionByAccountMutator.upsert(permission),
                subscriptionByAccountMutator.upsert(new SubscriptionAccount()
                        .setAccountId(permission.getAccountId())
                        .setSubscriptionId(permission.getSubscriptionId()))
        )).doOnError(throwable -> {
            log.error(String.format("error while saving subscription permission for account %s",
                    permission.getAccountId()), throwable);
            throw Exceptions.propagate(throwable);
        })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an account subscription permission
     *
     * @param permission the {@link AccountSubscriptionPermission} object to be deleted
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> deleteAccountPermission(AccountSubscriptionPermission permission) {
        return Mutators.execute(session, Flux.just(
                accountSubscriptionCollaboratorMutator.delete(new SubscriptionAccountCollaborator()
                        .setAccountId(permission.getAccountId())
                        .setSubscriptionId(permission.getSubscriptionId())),
                subscriptionPermissionByAccountMutator.delete(permission),
                subscriptionByAccountMutator.delete(new SubscriptionAccount()
                        .setAccountId(permission.getAccountId())
                        .setSubscriptionId(permission.getSubscriptionId()))
        )).doOnError(throwable -> {
            log.error(String.format("error while deleting subscription permission for account %s",
                    permission.getAccountId()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Save a team subscription permission
     *
     * @param permission the permission to save
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> saveTeamPermission(TeamSubscriptionPermission permission) {
        return Mutators.execute(session, Flux.just(
                teamSubscriptionPermissionMutator.upsert(permission),
                subscriptionByTeamMutator.upsert(new SubscriptionTeam()
                        .setSubscriptionId(permission.getSubscriptionId())
                        .setTeamId(permission.getTeamId())),
                teamSubscriptionCollaboratorMutator.upsert(new SubscriptionTeamCollaborator()
                        .setPermissionLevel(permission.getPermissionLevel())
                        .setSubscriptionId(permission.getSubscriptionId())
                        .setTeamId(permission.getTeamId()))
        )).doOnError(throwable -> {
            log.error(String.format("error while saving subscription permission for team %s",
                    permission.getTeamId()), throwable);
            throw Exceptions.propagate(throwable);
        })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a team subscription permission
     *
     * @param permission the permission to delete
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteTeamPermission(TeamSubscriptionPermission permission) {
        return Mutators.execute(session, Flux.just(
                teamSubscriptionPermissionMutator.delete(permission),
                subscriptionByTeamMutator.delete(new SubscriptionTeam()
                        .setTeamId(permission.getTeamId())
                        .setSubscriptionId(permission.getSubscriptionId())),
                teamSubscriptionCollaboratorMutator.delete(new SubscriptionTeamCollaborator()
                        .setTeamId(permission.getTeamId())
                        .setSubscriptionId(permission.getSubscriptionId()))
        ).doOnEach(ReactiveTransaction.linkOnNext())).doOnError(throwable -> {
            log.error(String.format("error while deleting subscription permission for team %s",
                    permission.getTeamId()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetches the permission level that an account has over a given subscription
     *
     * @param accountId the account requesting the permission level
     * @param subscriptionId the subscription representing the permission level entity
     * @return a {@link Mono} of subscription permission
     */
    public Mono<AccountSubscriptionPermission> fetchAccountPermission(UUID accountId, UUID subscriptionId) {
        return ResultSets.query(session, subscriptionPermissionByAccountMaterializer
                .fetchPermission(accountId, subscriptionId))
                .flatMapIterable(row -> row)
                .map(subscriptionPermissionByAccountMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching subscription permission for account %S and" +
                            " subscription %s", accountId, subscriptionId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches the permission level that a team has over a given subscription
     *
     * @param teamId the team requesting the permission level
     * @param subscriptionId the subscription representing the permission level entity
     * @return a {@link Mono} of permission level
     */
    public Mono<PermissionLevel> fetchTeamPermission(UUID teamId, UUID subscriptionId) {
        return ResultSets.query(session, teamSubscriptionPermissionMaterializer
        .fetchPermissionLevel(teamId, subscriptionId))
                .flatMapIterable(row->row)
                .map(teamSubscriptionPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching subscription permission for team %S and" +
                            " subscription %s", teamId, subscriptionId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches the team collaborators for a subscription
     *
     * @param subscriptionId the subscription to find the team collaborators for
     * @return a flux of team collaborators
     */
    @Trace(async = true)
    public Flux<SubscriptionTeamCollaborator> findTeamCollaborators(UUID subscriptionId) {
        return ResultSets.query(session, teamSubscriptionCollaboratorMaterializer.fetchCollaborators(subscriptionId))
                .flatMapIterable(row->row)
                .map(teamSubscriptionCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching team collaborators for subscription %s",
                            subscriptionId), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches the account collaborators for a subscription
     *
     * @param subscriptionId the subscription to find the account collaborators for
     * @return a flux of account collaborators
     */
    @Trace(async = true)
    public Flux<SubscriptionAccountCollaborator> findAccountCollaborators(UUID subscriptionId) {
        return ResultSets.query(session, accountSubscriptionCollaboratorMaterializer.fetchCollaborators(subscriptionId))
                .flatMapIterable(one->one)
                .map(accountSubscriptionCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching account collaborators for subscription %s",
                            subscriptionId), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Fetches the subscriptions for a team
     *
     * @param teamId the team to find subscriptions for
     * @return a flux of team collaborators
     */
    public Flux<UUID> findTeamSubscriptions(UUID teamId) {
        return ResultSets.query(session, subscriptionByTeamMaterializer.fetchSubscriptions(teamId))
                .flatMapIterable(row->row)
                .map(subscriptionByTeamMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching subscrptions for team %s",
                                            teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
