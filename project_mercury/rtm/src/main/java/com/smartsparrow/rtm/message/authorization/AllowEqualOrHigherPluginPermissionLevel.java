package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.plugin.AccountPluginPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.plugin.PluginPermissionMessage;

public class AllowEqualOrHigherPluginPermissionLevel implements AuthorizationPredicate<PluginPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowEqualOrHigherPluginPermissionLevel.class);

    private final PluginPermissionService pluginPermissionService;

    @Inject
    public AllowEqualOrHigherPluginPermissionLevel(PluginPermissionService pluginPermissionService) {
        this.pluginPermissionService = pluginPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    /**
     * Verify if a user is allowed to either grant/revoke a plugin permission.
     *
     * @param authenticationContext the context containing the authenticated account
     * @param message the incoming webSocket message
     * @return <code>true</code> if the action is permitted, or <code>false</code> when the action is not permitted
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, PluginPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingPermissionLevel = pluginPermissionService.findHighestPermissionLevel(account.getId(),
                message.getPluginId())
                .block();

        if (requestingPermissionLevel == null) {
            return false;
        }

        PermissionLevel targetPermissionLevel = getPermissionLevel(message);

        if (message.getPermissionLevel() != null) {
            return canGrant(message, requestingPermissionLevel, targetPermissionLevel);
        } else {
            return canRevoke(requestingPermissionLevel, targetPermissionLevel);
        }

    }

    /**
     * Check that the requesting user has a permission level higher than the target permission to allow him/her
     * to revoke the existing target permission
     *
     * @param requestingPermissionLevel the requester permission level
     * @param targetPermissionLevel the target permission level
     * @return <code>true</code> when the requester permission is higher than the target permission, <code>false</code>
     * when the target permission is higher than the requester permission
     */
    private boolean canRevoke(PermissionLevel requestingPermissionLevel, PermissionLevel targetPermissionLevel) {
        if (targetPermissionLevel == null) {
            return false;
        }

        return requestingPermissionLevel.isEqualOrHigherThan(targetPermissionLevel);
    }

    /**
     * Check that the requesting user is allowed to grant the permission.
     *
     * @param message the incoming webSocket message
     * @param requestingPermissionLevel the requester permission level
     * @param targetPermissionLevel the target permission level
     * @return <code>true</code> when the requester permission level is higher than the message permission and the existing
     * target permission (when found), or <code>false</code> when the requester permission level is lower
     */
    private boolean canGrant(PluginPermissionMessage message, PermissionLevel requestingPermissionLevel, PermissionLevel targetPermissionLevel) {
        if (targetPermissionLevel != null) {
            return requestingPermissionLevel.isEqualOrHigherThan(targetPermissionLevel)
                    && requestingPermissionLevel.isEqualOrHigherThan(message.getPermissionLevel());
        }

        return requestingPermissionLevel.isEqualOrHigherThan(message.getPermissionLevel());
    }

    /**
     * Defines the target permission level: permission level that is wanted to be granted or revoked.
     * @param message plugin permission message
     * @return the target permission level
     */
    private PermissionLevel getPermissionLevel(PluginPermissionMessage message) {
        PermissionLevel level;

        if (message.getAccountId() != null) {

            if (log.isDebugEnabled()) {
                log.debug("checking authorization for revoking permission to account {} for plugin {}", message.getAccountId(),
                        message.getPluginId());
            }
            level = pluginPermissionService.fetchAccountPermission(message.getAccountId(), message.getPluginId())
                    .map(AccountPluginPermission::getPermissionLevel).blockLast();

        } else if (message.getTeamId() != null) {

            if (log.isDebugEnabled()) {
                log.debug("checking authorization for revoking permission to team {} for plugin {}", message.getTeamId(),
                        message.getPluginId());
            }
            level = pluginPermissionService.fetchTeamPermission(message.getTeamId(), message.getPluginId()).block();

        } else {
            throw new IllegalArgumentException("permissionLevel, accountId or teamId should be provided in a message:" + message);
        }

        return level;
    }

}
