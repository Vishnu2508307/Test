package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.workspace.AccountWorkspacePermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.RevokeWorkspacePermissionMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

public class AllowRevokeEqualOrHigherWorkspacePermissionLevel implements AuthorizationPredicate<RevokeWorkspacePermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowRevokeEqualOrHigherWorkspacePermissionLevel.class);

    private final WorkspaceService workspaceService;

    @Inject
    public AllowRevokeEqualOrHigherWorkspacePermissionLevel(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Validates that the requesting account highest permission level is higher than the permission level the account is
     * trying to revoke. The authorizer the message to either supply a {@link RevokeWorkspacePermissionMessage#getAccountId()}
     * or {@link RevokeWorkspacePermissionMessage#getTeamId()} at leas one of those fields has to be supplied and not both
     * at the same time for the authorizer to work its logic. Breaking this rule will result in an unauthorized return.
     *
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, RevokeWorkspacePermissionMessage message) {

        Account account = authenticationContext.getAccount();
        PermissionLevel requestingPermission = workspaceService
                .findHighestPermissionLevel(account.getId(), message.getWorkspaceId()).block();

        if (requestingPermission != null) {
            if (message.getAccountId() != null && message.getTeamId() == null) {

                PermissionLevel targetAccountPermission = workspaceService.fetchPermission(message.getAccountId(), message.getWorkspaceId())
                        .map(AccountWorkspacePermission::getPermissionLevel)
                        .block();

                return targetAccountPermission != null && requestingPermission.isEqualOrHigherThan(targetAccountPermission);

            } else if (message.getAccountId() == null && message.getTeamId() != null) {
                // revoke the team id
                PermissionLevel targetTeamPermission = workspaceService
                        .fetchTeamPermission(message.getTeamId(), message.getWorkspaceId())
                        .block();

                return targetTeamPermission != null && requestingPermission.isEqualOrHigherThan(targetTeamPermission);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("could not authorize teamId {} and accountId {}", message.getTeamId(), message.getAccountId());
        }

        return false;
    }
}
