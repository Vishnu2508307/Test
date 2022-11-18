package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.plugin.PluginMessage;

public class AllowPluginReviewerOrHigher implements AuthorizationPredicate<PluginMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowPluginReviewerOrHigher.class);

    private final PluginPermissionService pluginPermissionService;

    @Inject
    public AllowPluginReviewerOrHigher(PluginPermissionService pluginPermissionService) {
        this.pluginPermissionService = pluginPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, PluginMessage message) {

        Account account = authenticationContext.getAccount();

        if (message.getPluginId() != null) {

            if (account != null) {
                PermissionLevel permission = pluginPermissionService
                        .findHighestPermissionLevel(account.getId(), message.getPluginId()).block();

                return permission != null
                        && permission.isEqualOrHigherThan(PermissionLevel.REVIEWER);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, `pluginId` was not supplied with the message {}", message);
        }

        return false;
    }
}
