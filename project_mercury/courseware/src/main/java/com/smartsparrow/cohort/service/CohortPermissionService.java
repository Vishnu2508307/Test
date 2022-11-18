package com.smartsparrow.cohort.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.cohort.data.AccountCohortCollaborator;
import com.smartsparrow.cohort.data.CohortAccount;
import com.smartsparrow.cohort.data.CohortGateway;
import com.smartsparrow.cohort.data.TeamCohortCollaborator;
import com.smartsparrow.iam.data.permission.cohort.AccountCohortPermission;
import com.smartsparrow.iam.data.permission.cohort.CohortPermissionGateway;
import com.smartsparrow.iam.data.permission.cohort.TeamCohortPermission;
import com.smartsparrow.iam.service.HighestPermissionLevel;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortPermissionService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CohortPermissionService.class);

    private final CohortPermissionGateway cohortPermissionGateway;
    private final CohortGateway cohortGateway;
    private final TeamService teamService;

    @Inject
    public CohortPermissionService(CohortPermissionGateway cohortPermissionGateway,
                                   CohortGateway cohortGateway,
                                   TeamService teamService) {
        this.cohortPermissionGateway = cohortPermissionGateway;
        this.cohortGateway = cohortGateway;
        this.teamService = teamService;
    }

    /**
     * Fetch account permission over a cohort
     *
     * @param accountId the account id to fetch the permission for
     * @param cohortId  the cohort entity the permission relates to
     * @return a {@link Mono} of {@link PermissionLevel}
     */
    public Mono<PermissionLevel> fetchAccountPermission(final UUID accountId, final UUID cohortId) {
        return cohortPermissionGateway.findAccountPermission(accountId, cohortId)
                .map(AccountCohortPermission::getPermissionLevel);
    }

    /**
     * Finds the highest permission level for the account over the cohort.
     * The methods finds both account and team specific permissions over the cohort,
     * then the highest permission level is returned.
     *
     * @param accountId the account id to search the permissions for
     * @param cohortId  the cohort id the account should have permission over
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findHighestPermissionLevel(final UUID accountId, final UUID cohortId) {
        return teamService.findTeamsForAccount(accountId)
                .flatMap(teamAccount -> fetchTeamPermission(teamAccount.getTeamId(), cohortId))
                .mergeWith(fetchAccountPermission(accountId, cohortId))
                .reduce(new HighestPermissionLevel());
    }

    /**
     * Fetch team permission over a cohort
     *
     * @param teamId   the team id to fetch the permission for
     * @param cohortId the cohort entity the permission relates to
     * @return a {@link Mono} of {@link PermissionLevel}
     */
    public Mono<PermissionLevel> fetchTeamPermission(final UUID teamId, final UUID cohortId) {
        return cohortPermissionGateway.findTeamPermission(teamId, cohortId)
                .map(TeamCohortPermission::getPermissionLevel);
    }

    /**
     * Save account permissions for cohort
     *
     * @param accountId       account id to be granted with permission
     * @param cohortId        cohort id
     * @param permissionLevel permission level
     */
    public Flux<Void> saveAccountPermissions(final UUID accountId, final UUID cohortId, final PermissionLevel permissionLevel) {
        checkNotNull(accountId, "accountId is required");
        checkNotNull(cohortId, "cohortId is required");
        checkNotNull(permissionLevel, "permissionLevel is required");

        return Flux.merge(
                cohortPermissionGateway.persist(new AccountCohortPermission()
                        .setAccountId(accountId)
                        .setCohortId(cohortId)
                        .setPermissionLevel(permissionLevel)),
                cohortGateway.persist(new CohortAccount()
                        .setAccountId(accountId)
                        .setCohortId(cohortId)),
                cohortGateway.persist(new AccountCohortCollaborator()
                        .setAccountId(accountId)
                        .setCohortId(cohortId)
                        .setPermissionLevel(permissionLevel)));
    }

    /**
     * Save multiple account permissions
     *
     * @param accountIds      the list of account ids to be granted with permission
     * @param cohortId        cohort id
     * @param permissionLevel the permission level
     */
    public Flux<Void> saveAccountPermissions(final List<UUID> accountIds, final UUID cohortId, final PermissionLevel permissionLevel) {
        checkNotNull(accountIds, "accountIds is required");
        return accountIds.stream().reduce(Flux.empty(),
                (flux, accountId) -> flux.mergeWith(saveAccountPermissions(accountId, cohortId, permissionLevel)),
                Flux::mergeWith);
    }

    /**
     * Save team permissions for cohort
     *
     * @param teamId          team id to be granted with permission
     * @param cohortId        cohort id
     * @param permissionLevel permission level
     */
    public Flux<Void> saveTeamPermissions(final UUID teamId, final UUID cohortId, final PermissionLevel permissionLevel) {
        checkNotNull(teamId, "teamId is required");
        checkNotNull(cohortId, "cohortId is required");
        checkNotNull(permissionLevel, "permissionLevel is required");

        return Flux.merge(
                cohortPermissionGateway.persist(new TeamCohortPermission()
                        .setTeamId(teamId)
                        .setCohortId(cohortId)
                        .setPermissionLevel(permissionLevel)),
                cohortGateway.persist(cohortId, teamId),
                cohortGateway.persist(new TeamCohortCollaborator()
                        .setTeamId(teamId)
                        .setCohortId(cohortId)
                        .setPermissionLevel(permissionLevel)));
    }

    /**
     * Save multiple team permissions
     *
     * @param teamIds         the list of team ids to be granted with permission
     * @param cohortId        cohort id
     * @param permissionLevel the permission level
     */
    public Flux<Void> saveTeamPermissions(final List<UUID> teamIds, final UUID cohortId, final PermissionLevel permissionLevel) {
        checkNotNull(teamIds, "teamIds is required");
        return teamIds.stream().reduce(Flux.empty(),
                (flux, teamId) -> flux.mergeWith(saveTeamPermissions(teamId, cohortId, permissionLevel)),
                Flux::mergeWith);
    }

    /**
     * Fetch all cohort permission for an account
     *
     * @param accountId the account to search the permission for
     * @return a flux stream of account cohort permission
     */
    public Flux<AccountCohortPermission> fetchPermissions(final UUID accountId) {
        return cohortPermissionGateway.findPermissions(accountId);
    }

    /**
     * Delete the account permissions over a cohort entity.
     *
     * @param accountId the account to delete the permission for
     * @param cohortId  the cohort the permission relates to
     */
    public Flux<Void> deleteAccountPermissions(final UUID accountId, final UUID cohortId) {
        checkNotNull(accountId, "accountId is required");
        checkNotNull(cohortId, "cohortId is required");

        return Flux.merge(
                cohortPermissionGateway.delete(new AccountCohortPermission()
                        .setAccountId(accountId)
                        .setCohortId(cohortId)),
                cohortGateway.delete(new CohortAccount()
                        .setAccountId(accountId)
                        .setCohortId(cohortId)),
                cohortGateway.delete(new AccountCohortCollaborator()
                        .setAccountId(accountId)
                        .setCohortId(cohortId))
        ).doOnError(throwable -> log.reactiveErrorThrowable(
                String.format("error while deleting permissions for account %s over cohort %s",
                        accountId, cohortId)));
    }

    /**
     * Delete the team permissions over a cohort entity.
     *
     * @param teamId   the team to delete the permission for
     * @param cohortId the cohort the permission relates to
     */
    public Flux<Void> deleteTeamPermissions(final UUID teamId, final UUID cohortId) {
        checkNotNull(teamId, "teamId is required");
        checkNotNull(cohortId, "cohortId is required");

        return Flux.merge(
                cohortPermissionGateway.delete(new TeamCohortPermission()
                        .setTeamId(teamId)
                        .setCohortId(cohortId)),
                cohortGateway.delete(cohortId, teamId),
                cohortGateway.delete(new TeamCohortCollaborator()
                        .setTeamId(teamId)
                        .setCohortId(cohortId))
        ).doOnError(throwable -> log.reactiveErrorThrowable(
                String.format("error while deleting permissions for team %s over cohort %s",
                        teamId, cohortId)));
    }
}
