package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.subscription.AccountSubscriptionPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.iam.RevokeSubscriptionPermissionMessage;

public class AllowRevokeEqualOrHigherSubscriptionPermissionLevel implements AuthorizationPredicate<RevokeSubscriptionPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowRevokeEqualOrHigherSubscriptionPermissionLevel.class);

    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AllowRevokeEqualOrHigherSubscriptionPermissionLevel(SubscriptionPermissionService subscriptionPermissionService) {
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, RevokeSubscriptionPermissionMessage message) {
        Account account = authenticationContext.getAccount();
        PermissionLevel requestingPermission = subscriptionPermissionService
                .findHighestPermissionLevel(account.getId(), message.getSubscriptionId()).block();

        if (requestingPermission != null) {
            if (message.getAccountId() != null && message.getTeamId() == null) {

                PermissionLevel targetAccountPermission = subscriptionPermissionService
                        .findAccountPermission(message.getAccountId(), message.getSubscriptionId())
                        .map(AccountSubscriptionPermission::getPermissionLevel)
                        .block();

                return targetAccountPermission != null && requestingPermission.isEqualOrHigherThan(targetAccountPermission);

            } else if (message.getAccountId() == null && message.getTeamId() != null) {
                // revoke the team id
                PermissionLevel targetTeamPermission = subscriptionPermissionService
                        .findTeamPermission(message.getTeamId(), message.getSubscriptionId())
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
