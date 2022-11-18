package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.ReceivedMessage;

public class AllowSelfSubscriptionContributorOrHigher implements AuthorizationPredicate<ReceivedMessage> {

    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AllowSelfSubscriptionContributorOrHigher(SubscriptionPermissionService subscriptionPermissionService) {
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Checks that the logged in user has {@link PermissionLevel#CONTRIBUTOR} or higher over his/her own subscription
     *
     * @param authenticationContext the authentication context holding the account
     * @param receivedMessage the message received on the socket
     * @return <code>true</code> when the permission level over the subscription is {@link PermissionLevel#CONTRIBUTOR}
     *         or higher.
     *         <code>false</code> when the permission level is not found or lower than {@link PermissionLevel#CONTRIBUTOR}
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, ReceivedMessage receivedMessage) {

        Account account = authenticationContext.getAccount();

        if (account != null) {
            PermissionLevel permission = subscriptionPermissionService
                    .findHighestPermissionLevel(account.getId(), account.getSubscriptionId()).block();

            return permission != null && permission.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
        }

        return false;
    }
}
