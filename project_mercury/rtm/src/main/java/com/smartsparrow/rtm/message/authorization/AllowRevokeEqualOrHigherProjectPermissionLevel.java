package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.RevokeProjectPermissionMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

public class AllowRevokeEqualOrHigherProjectPermissionLevel implements AuthorizationPredicate<RevokeProjectPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowRevokeEqualOrHigherProjectPermissionLevel.class);

    private final ProjectPermissionService projectPermissionService;

    @Inject
    public AllowRevokeEqualOrHigherProjectPermissionLevel(final ProjectPermissionService projectPermissionService) {
        this.projectPermissionService = projectPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    /**
     * Verify that the user that is revoking the permission level has an equal or higher permission
     * than the one he/she is trying to revoke.
     *
     * @param authenticationContext holds the authenticated user
     * @param message               the message to authorize
     * @return <code>false</code> when the permission level of the requesting user is lower than the permission
     * being revoked
     * <code>true</code> when the permission level of the requesting user is equal or higher than the permission
     * being revoked
     */
    @Override
    public boolean test(final AuthenticationContext authenticationContext, final RevokeProjectPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingAccountPermission = projectPermissionService.findHighestPermissionLevel(account.getId(),
                message.getProjectId()).block();

        if (log.isDebugEnabled()) {
            log.debug("checking authorization for revoking permission for project {}", message);
        }
        PermissionLevel targetPermission;
        if (message.getAccountId() != null) {
            targetPermission = projectPermissionService.fetchAccountPermission(message.getAccountId(), message.getProjectId())
                    .block();
        } else {
            targetPermission = projectPermissionService.fetchTeamPermission(message.getTeamId(), message.getProjectId())
                    .block();
        }

        if (targetPermission == null) {
            if (log.isDebugEnabled()) {
                log.debug("target permission level not found for project {}", message);
            }
            return false;
        }

        return requestingAccountPermission != null && requestingAccountPermission.isEqualOrHigherThan(targetPermission);
    }
}
