package com.smartsparrow.rtm.message.authorization;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.data.permission.cohort.AccountCohortPermission;
import com.smartsparrow.iam.data.permission.cohort.TeamCohortPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.cohort.GrantCohortPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This authorizer is used when a permission over a cohort entity is granted to an account.
 */
public class AllowGrantEqualOrHigherCohortPermissionLevel implements AuthorizationPredicate<GrantCohortPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowGrantEqualOrHigherCohortPermissionLevel.class);

    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowGrantEqualOrHigherCohortPermissionLevel(CohortPermissionService cohortPermissionService) {
        this.cohortPermissionService = cohortPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    /**
     * Verify that the user that is granting the permission level has an equal or higher permission
     * than the one he/she is trying to grant.
     *
     * @param authenticationContext holds the authenticated user
     * @param message               the message to authorize
     * @return <code>false</code> when the permission level of the requesting user is lower than the permission
     * being granted
     * <code>true</code> when the permission level of the requesting user is equal or higher than the permission
     * being granted
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, GrantCohortPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingPermission = cohortPermissionService
                .findHighestPermissionLevel(account.getId(), message.getCohortId())
                .block();

        if (requestingPermission == null) {
            return false;
        }

        List<UUID> notAllowedTargetPermission = getNotPermitted(message, requestingPermission)
                .block();

        if (notAllowedTargetPermission != null && !notAllowedTargetPermission.isEmpty()) {
            String entity = message.getAccountIds() != null ? "accounts" : "teams";
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s %s have an higher permission level over cohort %s that cannot be overridden by %s",
                        entity,
                        notAllowedTargetPermission.toString(),
                        message.getCohortId(),
                        account.getId()));
            }
            // at least one account in the list has an higher permission level than the requesting account
            return false;
        }

        return requestingPermission.isEqualOrHigherThan(message.getPermissionLevel());
    }

    /**
     * Get a list of ids that have an higher permission level over the cohort and are not permitted to be overridden
     *
     * @param message the incoming webSocket message
     * @param requestingPermission the requester permission level
     * @return a list of ids that are not allowed to have their permission level overridden
     */
    private Mono<List<UUID>> getNotPermitted(GrantCohortPermissionMessage message, PermissionLevel requestingPermission) {
        if (message.getAccountIds() != null) {
            return getAccountsPermissionLevelFor(message.getAccountIds(), message.getCohortId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(AccountCohortPermission::getAccountId)
                    .collectList();
        } else {
            return getTeamsPermissionLevelFor(message.getTeamIds(), message.getCohortId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(TeamCohortPermission::getTeamId)
                    .collectList();

        }
    }

    /**
     * Find the team cohort permission for each team id in the list
     *
     * @param teamIds the team ids to find the permissions for
     * @param cohortId the cohort the permissions refer to
     * @return a flux of team cohort permission
     */
    private Flux<TeamCohortPermission> getTeamsPermissionLevelFor(List<UUID> teamIds, final UUID cohortId) {
        return Flux.just(teamIds.toArray(new UUID[0]))
                .flatMap(teamId -> cohortPermissionService.fetchTeamPermission(teamId, cohortId)
                        .flatMap(permissionLevel -> Mono.just(new TeamCohortPermission()
                                .setTeamId(teamId)
                                .setCohortId(cohortId)
                                .setPermissionLevel(permissionLevel))));
    }

    /**
     * Find the account cohort permission for each account in the list
     *
     * @param accountIds the account ids to find the permissions for
     * @param cohortId the cohort the permissions refer to
     * @return a flux of account cohort permission
     */
    private Flux<AccountCohortPermission> getAccountsPermissionLevelFor(List<UUID> accountIds, final UUID cohortId) {
        return Flux.just(accountIds.toArray(new UUID[0]))
                .flatMap(accountId -> cohortPermissionService.fetchAccountPermission(accountId, cohortId)
                        .flatMap(permissionLevel -> Mono.just(new AccountCohortPermission()
                                .setAccountId(accountId)
                                .setCohortId(cohortId)
                                .setPermissionLevel(permissionLevel))));
    }
}
