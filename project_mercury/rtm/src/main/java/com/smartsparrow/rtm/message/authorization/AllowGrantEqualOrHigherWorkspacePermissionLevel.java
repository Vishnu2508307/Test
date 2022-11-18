package com.smartsparrow.rtm.message.authorization;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.workspace.AccountWorkspacePermission;
import com.smartsparrow.iam.data.permission.workspace.TeamWorkspacePermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.GrantWorkspacePermissionMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This authorizer is used when a permission over a workspace entity is either granted or revoked from an account.
 */
public class AllowGrantEqualOrHigherWorkspacePermissionLevel implements AuthorizationPredicate<GrantWorkspacePermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowGrantEqualOrHigherWorkspacePermissionLevel.class);

    private final WorkspaceService workspaceService;

    @Inject
    public AllowGrantEqualOrHigherWorkspacePermissionLevel(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Verify that the user granting the permission level has an equal or higher permission
     * than the one he/she is trying to grant.
     * @param authenticationContext holds the authenticated user
     * @param message the message to authorize
     * @return <code>false</code> when the permission level of the requesting user is lower than the permission
     * being granted/revoked
     * <code>true</code> when the permission level of the requesting user is equal or higher than the permission
     * being granted/revoked
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, GrantWorkspacePermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingPermission = workspaceService
                .findHighestPermissionLevel(account.getId(), message.getWorkspaceId()).block();

        if (requestingPermission == null) {
            return false;
        }

        List<UUID> notAllowedTargetPermission = getNotPermitted(message, requestingPermission)
                .block();

        if (notAllowedTargetPermission != null && !notAllowedTargetPermission.isEmpty()) {
            String entity = message.getAccountIds() != null ? "accounts" : "teams";
            if (log.isDebugEnabled()) {
                log.warn(String.format("%s %s have an higher permission level over workspace %s that cannot be overridden by %s",
                        entity,
                        notAllowedTargetPermission.toString(),
                        message.getWorkspaceId(),
                        account.getId()));
            }
            // at least one account in the list has an higher permission level than the requesting account
            return false;
        }

        // all accounts in the list have either no permission at all or a lower permission than the requesting account
        return requestingPermission.isEqualOrHigherThan(message.getPermissionLevel());
    }

    /**
     * Get a list of ids that have an higher permission level than the requester. The id represents either an accountId
     * or a teamId
     *
     * @param message the incoming webSocket message
     * @param requestingPermission the requester permission level
     * @return a list of ids that are not allowed to be overridden
     */
    private Mono<List<UUID>> getNotPermitted(GrantWorkspacePermissionMessage message, final PermissionLevel requestingPermission) {

        if (message.getAccountIds() != null) {
            return getAccountsPermissionLevelFor(message.getAccountIds(), message.getWorkspaceId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(AccountWorkspacePermission::getAccountId)
                    .collectList();
        } else {
            return getTeamsPermissionLevelFor(message.getTeamIds(), message.getWorkspaceId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(TeamWorkspacePermission::getTeamId)
                    .collectList();
        }
    }

    /**
     * Find all the team workspace permission for each team id in the list
     *
     * @param teamIds the team ids to find the permission for
     * @param workspaceId the workspace the team permission refers to
     * @return a flux of team workspace permission
     */
    private Flux<TeamWorkspacePermission> getTeamsPermissionLevelFor(List<UUID> teamIds, final UUID workspaceId) {
        return Flux.just(teamIds.toArray(new UUID[0]))
                .flatMap(teamId -> workspaceService.fetchTeamPermission(teamId, workspaceId)
                        .flatMap(permissionLevel -> Mono.just(new TeamWorkspacePermission()
                                .setPermissionLevel(permissionLevel)
                                .setTeamId(teamId)
                                .setWorkspaceId(workspaceId))));
    }

    /**
     * Find all the account workspace permission for each account id in the list
     *
     * @param accountIds the account ids to find the permission for
     * @param workspaceId the workspace id the permission refers to
     * @return a flux of account workspace permission
     */
    private Flux<AccountWorkspacePermission> getAccountsPermissionLevelFor(List<UUID> accountIds, final UUID workspaceId) {
        return Flux.just(accountIds.toArray(new UUID[0]))
                .flatMap(accountId -> workspaceService.fetchPermission(accountId, workspaceId));
    }

}
