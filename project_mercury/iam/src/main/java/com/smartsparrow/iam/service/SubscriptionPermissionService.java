package com.smartsparrow.iam.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.data.SubscriptionAccountCollaborator;
import com.smartsparrow.iam.data.SubscriptionTeamCollaborator;
import com.smartsparrow.iam.data.permission.subscription.AccountSubscriptionPermission;
import com.smartsparrow.iam.data.permission.subscription.SubscriptionPermissionGateway;
import com.smartsparrow.iam.data.permission.subscription.TeamSubscriptionPermission;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class SubscriptionPermissionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPermissionService.class);

    private final SubscriptionPermissionGateway subscriptionPermissionGateway;
    private final TeamService teamService;

    @Inject
    public SubscriptionPermissionService(SubscriptionPermissionGateway subscriptionPermissionGateway,
                                         TeamService teamService) {
        this.subscriptionPermissionGateway = subscriptionPermissionGateway;
        this.teamService = teamService;
    }

    /**
     * Save an account subscription permission
     *
     * @param accountId       the accountId that has the permission
     * @param subscriptionId  the target subscription id for the permission
     * @param permissionLevel the {@link PermissionLevel}
     * @return a {@link Flux} of {@link Void}
     * @throws IllegalArgumentException when any of the supplied parameter is null
     */
    @Trace(async = true)
    public Flux<Void> saveAccountPermission(UUID accountId, UUID subscriptionId, PermissionLevel permissionLevel)
            throws IllegalArgumentException {

        checkArgument(accountId != null, "accountId is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");
        checkArgument(permissionLevel != null, "permissionLevel is required");

        return subscriptionPermissionGateway.saveAccountPermission(new AccountSubscriptionPermission()
                .setAccountId(accountId)
                .setSubscriptionId(subscriptionId)
                .setPermissionLevel(permissionLevel))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an account subscription permission
     *
     * @param accountId       the accountId that has the permission
     * @param subscriptionId  the target subscription id for the permission
     * @return a {@link Flux} of {@link Void}
     * @throws IllegalArgumentException when any of the supplied parameter is <code>null</code>
     */
    public Flux<Void> deleteAccountPermission(UUID accountId, UUID subscriptionId)
            throws IllegalArgumentException {

        checkArgument(accountId != null, "accountId is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");

        return subscriptionPermissionGateway.deleteAccountPermission(new AccountSubscriptionPermission()
                .setAccountId(accountId)
                .setSubscriptionId(subscriptionId));
    }

    /**
     * Save a team subscription permission
     *
     * @param teamId the team id to save the permission for
     * @param subscriptionId the subscription the team will have permission over
     * @param permissionLevel the permission level
     * @return a flux of void
     * @throws IllegalArgumentException when any of the required parameter is <code>null</code>
     */
    @Trace(async = true)
    public Flux<Void> saveTeamPermission(UUID teamId, UUID subscriptionId, PermissionLevel permissionLevel)
            throws IllegalArgumentException {

        checkArgument(teamId != null, "teamId is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");
        checkArgument(permissionLevel != null, "permissionLevel is required");

        return subscriptionPermissionGateway.saveTeamPermission(new TeamSubscriptionPermission()
                .setPermissionLevel(permissionLevel)
                .setTeamId(teamId)
                .setSubscriptionId(subscriptionId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a team subscription permission
     *
     * @param teamId the team id to revoke the permission for
     * @param subscriptionId the subscription that the team will lose permission over
     * @return a flux of void
     * @throws IllegalArgumentException when any of the required parameter is <code>null</code>
     */
    @Trace(async = true)
    public Flux<Void> deleteTeamPermission(UUID teamId, UUID subscriptionId)
            throws IllegalArgumentException{

        checkArgument(teamId != null, "teamId is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");

        return subscriptionPermissionGateway.deleteTeamPermission(new TeamSubscriptionPermission()
                .setTeamId(teamId)
                .setSubscriptionId(subscriptionId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches the permission level that a team has over a subscription
     *
     * @param teamId the team to search the permission for
     * @param subscriptionId the subscription id the team should have permission over
     * @return a mono of permission level or an empty mono when the permission is not found
     */
    public Mono<PermissionLevel> findTeamPermission(UUID teamId, UUID subscriptionId) {
        return subscriptionPermissionGateway.fetchTeamPermission(teamId, subscriptionId);
    }


    /**
     * Fetches the account {@link AccountSubscriptionPermission} over a subscription entity.
     *
     * @param accountId      the account requesting the permission level
     * @param subscriptionId the target subscription
     * @return a {@link Mono} of subscription permission
     */
    public Mono<AccountSubscriptionPermission> findAccountPermission(UUID accountId, UUID subscriptionId) {
        return subscriptionPermissionGateway.fetchAccountPermission(accountId, subscriptionId);
    }

    /**
     * Finds all the permission the account has over the subscription. The methods finds both account and team specific
     * permissions over the subscription, then the highest permission level is returned.
     *
     * @param accountId the account id to search the permissions for
     * @param subscriptionId the subscription id the account should have permission over
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findHighestPermissionLevel(UUID accountId, UUID subscriptionId) {
        return teamService.findTeamsForAccount(accountId)
                .onErrorResume(ex->{
                    if (log.isDebugEnabled()) {
                        log.debug("could not fetch teams for account {} {}", accountId, ex.getMessage());
                    }
                    return Mono.empty();
                })
                .map(teamAccount -> findTeamPermission(teamAccount.getTeamId(), subscriptionId))
                .flatMap(one -> one)
                .mergeWith(findAccountPermission(accountId, subscriptionId)
                        .map(AccountSubscriptionPermission::getPermissionLevel))
                .reduce(new HighestPermissionLevel());
    }

    /**
     * Find team collaborators for a subscription
     */
    @Trace(async = true)
    public Flux<SubscriptionTeamCollaborator> findTeamCollaborators(UUID subscriptionId) {
        return subscriptionPermissionGateway.findTeamCollaborators(subscriptionId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find account collaborators for a subscription
     */
    @Trace(async = true)
    public Flux<SubscriptionAccountCollaborator> findAccountCollaborators(UUID subscriptionId) {
        return subscriptionPermissionGateway.findAccountCollaborators(subscriptionId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find subscriptions for a team
     */
    @Trace(async = true)
    public Flux<UUID> findTeamSubscriptions(UUID teamId) {
        return subscriptionPermissionGateway.findTeamSubscriptions(teamId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
