package com.smartsparrow.iam.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.data.permission.team.TeamPermissionGateway;
import com.smartsparrow.iam.data.team.AccountTeamCollaborator;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.data.team.TeamBySubscription;
import com.smartsparrow.iam.data.team.TeamGateway;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.lang.PermissionNotFoundException;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.payload.TeamPayload;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Service class for Team related operations
@Singleton
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final TeamGateway teamGateway;
    private final TeamPermissionGateway teamPermissionGateway;
    private final AccountService accountService;

    @Inject
    public TeamService(TeamGateway teamGateway,
                       TeamPermissionGateway teamPermissionGateway,
                       AccountService accountService) {
        this.teamGateway = teamGateway;
        this.teamPermissionGateway = teamPermissionGateway;
        this.accountService = accountService;
    }

    /**
     * Create a team inside a subscription
     *
     * @param creatorId      account who creates a team
     * @param name           team name
     * @param description    team description, optional
     * @param thumbnail      team thumbnail, optional
     * @param subscriptionId subscription for which the team should be created
     * @return Mono<TeamSummary>{@link Mono<TeamSummary>} created team
     * @throws IllegalArgumentException - when the required arguments are null
     */
    @Trace(async = true)
    public Mono<TeamSummary> createTeam(final UUID creatorId,
                                        final String name,
                                        final String description,
                                        final String thumbnail,
                                        final UUID subscriptionId) {
        checkArgument(creatorId != null, "creatorId is required");
        checkArgument(name != null, "name is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");

        UUID teamId = UUIDs.timeBased();
        TeamSummary teamSummary = new TeamSummary()
                .setId(teamId)
                .setName(name)
                .setDescription(description)
                .setSubscriptionId(subscriptionId)
                .setThumbnail(thumbnail);

        TeamBySubscription teamBySubscription = new TeamBySubscription()
                .setSubscriptionId(subscriptionId)
                .setTeamId(teamId);

        return teamGateway.persist(teamSummary)
                .then(teamGateway.persist(teamBySubscription))
                .thenMany(savePermission(creatorId, teamId, PermissionLevel.OWNER))
                .then(Mono.just(teamSummary))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("error while creating a team summary with %s", teamSummary), e);
                    }
                    throw Exceptions.propagate(e);
                });
    }

    /**
     * Update a team details. If parameter is null it will not be updated. Only non null fields will be stored.
     *
     * @param teamId      team to update
     * @param name        new name, can be null
     * @param description new description, can be null
     * @param thumbnail   new thumbnail, can be null
     * @return Mono<TeamSummary>{@link Mono<TeamSummary>}
     * @throws IllegalArgumentException - When teamId is null
     */
    @Trace(async = true)
    public Mono<Void> updateTeam(final UUID teamId,
                                 final String name,
                                 final String description,
                                 final String thumbnail) {
        checkArgument(teamId != null, "teamId is required");

        TeamSummary teamSummary = new TeamSummary()
                .setId(teamId)
                .setName(name)
                .setDescription(description)
                .setThumbnail(thumbnail);

        return teamGateway.persist(teamSummary)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("error while updating a team summary with %s", teamSummary), e);
                    throw Exceptions.propagate(e);
                });
    }

    /**
     * Delete a team account
     *
     * @param teamId         team to delete from
     * @param accountId      account id being deleted from team
     * @return Flux<Void>{@link Flux<Void>}
     * @throws IllegalArgumentException - when the required arguments are null
     */
    @Trace(async = true)
    public Flux<Void> deleteTeamAccount(final UUID teamId,
                                        final UUID accountId) {
        checkArgument(teamId != null, "teamId is required");
        checkArgument(accountId != null, "accountId is required");

        return Flux.merge(
                teamPermissionGateway.delete(new TeamPermission()
                       .setAccountId(accountId)
                       .setTeamId(teamId)),
                teamGateway.delete(new TeamAccount()
                       .setAccountId(accountId)
                       .setTeamId(teamId)),
                teamGateway.delete(new AccountTeamCollaborator()
                       .setAccountId(accountId)
                       .setTeamId(teamId))
        ).doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Delete a team subscription
     *
     * @param teamId              team to delete from
     * @param subscriptionId      subscription being deleted from team
     * @return Flux<Void>{@link Flux<Void>}
     * @throws IllegalArgumentException - when the required arguments are null
     */
    @Trace(async = true)
    public Flux<Void> deleteTeamSubscription(final UUID teamId,
                                             final UUID subscriptionId) {
        checkArgument(teamId != null, "teamId is required");
        checkArgument(subscriptionId != null, "subscriptionId is required");

        return teamGateway.delete(new TeamBySubscription().setTeamId(teamId).setSubscriptionId(subscriptionId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Delete a team
     *
     * @param teamId         team to delete
     * @return Flux<Void>{@link Flux<Void>}
     * @throws IllegalArgumentException - when the required arguments are null
     */
    @Trace(async = true)
    public Flux<Void> deleteTeam(final UUID teamId) {
        checkArgument(teamId != null, "teamId is required");

        return teamGateway.delete(new TeamSummary().setId(teamId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a team by its id
     *
     * @param teamId
     * @return Mono<TeamSummary>{@link Mono<TeamSummary>}
     * @throws IllegalArgumentException
     */
    @Trace(async = true)
    public Mono<TeamSummary> findTeam(final UUID teamId) {
        checkArgument(teamId != null, "teamId is required");

        return teamGateway.findTeam(teamId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all teams by subscription id
     *
     * @param subscriptionId
     * @return Flux<TeamBySubscription>{@link Flux<TeamBySubscription>}
     * @throws IllegalArgumentException
     */
    @Trace(async = true)
    public Flux<TeamBySubscription> findAllTeamsBySubscription(final UUID subscriptionId) {
        checkArgument(subscriptionId != null, "subscriptionId is required");

        return teamGateway.findTeamsForSubscription(subscriptionId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all accounts for a team
     *
     * @param teamId
     * @return Flux<AccountTeamCollaborator>{@link Flux<AccountTeamCollaborator>}
     * @throws IllegalArgumentException
     */
    @Trace(async = true)
    public Flux<AccountTeamCollaborator> findAllCollaboratorsForATeam(final UUID teamId) {
        checkArgument(teamId != null, "teamId is required");

        return teamGateway.findAccountCollaborators(teamId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all teams for an account
     *
     * @param accountId
     * @return Flux<TeamAccount>{@link Flux<TeamAccount>}
     * @throws IllegalArgumentException
     */
    @Trace(async = true)
    public Flux<TeamAccount> findTeamsForAccount(final UUID accountId) {
        checkArgument(accountId != null, "accountId is required");

        return teamGateway.findTeamsForAccount(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Add account and permission to a team
     *
     * @param accountId
     * @param teamId
     * @param permissionLevel
     * @return Flux<Void>{@link Flux<Void>}
     * @throws IllegalArgumentException
     */
    @Trace(async = true)
    public Flux<Void> savePermission(final UUID accountId, final UUID teamId, final PermissionLevel permissionLevel) {
        checkArgument(accountId != null, "accountId is required");
        checkArgument(teamId != null, "teamId is required");
        checkArgument(permissionLevel != null, "permissionLevel is required");

        return Flux.merge(
                teamPermissionGateway.persist(new TeamPermission()
                        .setTeamId(teamId)
                        .setAccountId(accountId)
                        .setPermissionLevel(permissionLevel)),

                teamGateway.persist(new AccountTeamCollaborator()
                        .setAccountId(accountId)
                        .setTeamId(teamId)
                        .setPermissionLevel(permissionLevel)),

                teamGateway.persist(new TeamAccount()
                        .setAccountId(accountId)
                        .setTeamId(teamId)))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Delete account permissions for a team
     *
     * @param accountId
     * @param teamId
     * @return Flux<Void>{@link Flux<Void>}
     * @throws IllegalArgumentException
     */
    @Trace(async = true)
    public Flux<Void> deletePermission(final UUID accountId, final UUID teamId) {
        checkArgument(accountId != null, "accountId is required");
        checkArgument(teamId != null, "teamId is required");

        return Flux.merge(teamPermissionGateway.delete(new TeamPermission()
                        .setAccountId(accountId)
                        .setTeamId(teamId)),

                teamGateway.delete(new AccountTeamCollaborator()
                        .setTeamId(teamId)
                        .setAccountId(accountId)),

                teamGateway
                        .delete(new TeamAccount()
                                .setTeamId(teamId)
                                .setAccountId(accountId)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch an account's permission within the team
     *
     * @param accountId
     * @param teamId
     * @return Mono<TeamPermission>{@link Mono<TeamPermission>}
     * @throws IllegalArgumentException
     */
    public Mono<TeamPermission> fetchPermission(final UUID accountId, final UUID teamId) {
        checkArgument(accountId != null, "accountId is required");
        checkArgument(teamId != null, "teamId is required");

        return teamPermissionGateway.findPermission(accountId, teamId);
    }

    /**
     * Fetches permissions for a list of accounts within in a team
     * @param accountIds
     * @param teamId
     * @return Flux<TeamPermission>{@link Flux<TeamPermission>}
     * @throws IllegalArgumentException
     */
    public  Flux<TeamPermission> fetchPermissions(final List<UUID> accountIds, final UUID teamId){
        checkArgument(accountIds != null, "accountIds is required");
        checkArgument(teamId != null, "teamId is required");

        return accountIds
                .stream()
                .map(accountId -> fetchPermission(accountId, teamId).flux())
                .reduce((prev, next) -> Flux.merge(prev, next))
                .orElseThrow(() -> new PermissionNotFoundException(
                        String.format("Permission not found for accounts %s for team %s",accountIds,teamId)));

    }

    /**
     * Get the Team Payload for a team
     *
     * @param teamId                - The unique identifier for a team
     * @param collaboratorsLimit - The limit for returning account summaries
     * @return Mono<TeamPayload> {@link Mono<TeamPayload>}
     */
    @Trace(async = true)
    public Mono<TeamPayload> getTeamPayload(final UUID teamId, final Integer collaboratorsLimit) {

        checkArgument(teamId != null, "teamId is required");
        checkArgument(collaboratorsLimit != null, "collaboratorTeamLimit is required");

        Flux<UUID> collaboratorIds = teamGateway
                .findAccountCollaborators(teamId)
                .map(AccountTeamCollaborator::getAccountId)
                .doOnEach(ReactiveTransaction.linkOnNext()); // Find all accounts that belong to a team

        Mono<List<AccountSummaryPayload>> accountSummariesMono = accountService
                .getAccountSummaryPayloads(collaboratorsLimit, collaboratorIds)
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<TeamSummary> teamSummaryMono = teamGateway.findTeam(teamId)
                .doOnEach(ReactiveTransaction.linkOnNext()); // Get the team summary
        Mono<Long> countMono = collaboratorIds.count()
                .doOnEach(ReactiveTransaction.linkOnNext()); // Count of the accounts that belong to a team

        return Mono.zip(teamSummaryMono, accountSummariesMono, countMono) //Zip all 3 components to build TeamPayload
                .map(tuple3 -> TeamPayload.from(tuple3.getT1().getId(), tuple3.getT1().getName(), tuple3.getT2(), tuple3.getT3().intValue()));
    }

    /**
     * Builds a payload object for team collaborator. The result contains team details and team's permission level.
     * Team details are fetched from database by teamId.
     * @param teamId id of the team to build payload object for
     * @param permissionLevel permission level the team has, can be null
     * @return a created payload object
     */
    @Trace(async = true)
    public Mono<TeamCollaboratorPayload> getTeamCollaboratorPayload(final UUID teamId, final PermissionLevel permissionLevel) {
        checkArgument(teamId != null, "teamId is required");
        return findTeam(teamId)
                .map(team -> TeamCollaboratorPayload.from(team, permissionLevel))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

}
