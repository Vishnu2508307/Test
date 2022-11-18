package com.smartsparrow.rtm.message.authorization;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.workspace.AccountProjectPermission;
import com.smartsparrow.iam.data.permission.workspace.TeamProjectPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.GrantProjectPermissionMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AllowGrantEqualOrHigherProjectPermissionLevel implements AuthorizationPredicate<GrantProjectPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowGrantEqualOrHigherProjectPermissionLevel.class);

    private final ProjectPermissionService projectPermissionService;

    @Inject
    public AllowGrantEqualOrHigherProjectPermissionLevel(final ProjectPermissionService projectPermissionService) {
        this.projectPermissionService = projectPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    @Override
    public boolean test(final AuthenticationContext authenticationContext, final GrantProjectPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingPermission = projectPermissionService
                .findHighestPermissionLevel(account.getId(), message.getProjectId())
                .block();

        if (requestingPermission == null) {
            return false;
        }

        List<UUID> notAllowedTargetPermission = getNotPermitted(message, requestingPermission)
                .block();

        if (notAllowedTargetPermission != null && !notAllowedTargetPermission.isEmpty()) {
            String entity = message.getAccountIds() != null ? "accounts" : "teams";
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s %s have an higher permission level over the project %s that cannot be overridden by %s",
                        entity,
                        notAllowedTargetPermission.toString(),
                        message.getProjectId(),
                        account.getId()));
            }
            // at least one account in the list has an higher permission level than the requesting account
            return false;
        }

        return requestingPermission.isEqualOrHigherThan(message.getPermissionLevel());    }

    private Mono<List<UUID>> getNotPermitted(final GrantProjectPermissionMessage message, final PermissionLevel requestingPermission) {
        if (message.getAccountIds() != null) {
            return getAccountsPermissionLevelFor(message.getAccountIds(), message.getProjectId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(AccountProjectPermission::getAccountId)
                    .collectList();
        } else {
            return getTeamsPermissionLevelFor(message.getTeamIds(), message.getProjectId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(TeamProjectPermission::getTeamId)
                    .collectList();

        }
    }

    /**
     * Find the team project permission for each team id in the list
     *
     * @param teamIds the team ids to find the permissions for
     * @param projectId the project the permissions refer to
     * @return a flux of team project permission
     */
    private Flux<TeamProjectPermission> getTeamsPermissionLevelFor(List<UUID> teamIds, final UUID projectId) {
        return Flux.just(teamIds.toArray(new UUID[0]))
                .flatMap(teamId -> projectPermissionService.fetchTeamPermission(teamId, projectId)
                        .flatMap(permissionLevel -> Mono.just(new TeamProjectPermission()
                                .setTeamId(teamId)
                                .setProjectId(projectId)
                                .setPermissionLevel(permissionLevel))));
    }

    /**
     * Find the account project permission for each account in the list
     *
     * @param accountIds the account ids to find the permissions for
     * @param projectId the project the permissions refer to
     * @return a flux of account project permission
     */
    private Flux<AccountProjectPermission> getAccountsPermissionLevelFor(List<UUID> accountIds, final UUID projectId) {
        return Flux.just(accountIds.toArray(new UUID[0]))
                .flatMap(accountId -> projectPermissionService.fetchAccountPermission(accountId, projectId)
                        .flatMap(permissionLevel -> Mono.just(new AccountProjectPermission()
                                .setAccountId(accountId)
                                .setProjectId(projectId)
                                .setPermissionLevel(permissionLevel))));
    }
}
