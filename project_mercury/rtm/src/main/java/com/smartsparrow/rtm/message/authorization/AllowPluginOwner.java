package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.plugin.PluginMessage;

/**
 * Currently this authorizer is not used, leaving it since it can be useful in the near future.
 * TODO: remove this if not used in a reasonable amount of time
 */
public class AllowPluginOwner implements AuthorizationPredicate<PluginMessage> {

    private final PluginPermissionService pluginPermissionService;

    @Inject
    public AllowPluginOwner(PluginPermissionService pluginPermissionService) {
        this.pluginPermissionService = pluginPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return String.format("%s permission level required", PermissionLevel.OWNER.name());
    }

    /**
     * Checks that the logged user has {@link PermissionLevel#OWNER} over the supplied pluginId.
     *
     * @param authenticationContext the context containing the logged user
     * @param pluginMessage the received message
     * @return <code>true</code> when the logged user has {@link PermissionLevel#OWNER} over
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, PluginMessage pluginMessage) {

        Account account = authenticationContext.getAccount();

        PermissionLevel permission = pluginPermissionService.findHighestPermissionLevel(account.getId(),
                pluginMessage.getPluginId()).block();

        return permission != null && permission.equals(PermissionLevel.OWNER);
    }
}
