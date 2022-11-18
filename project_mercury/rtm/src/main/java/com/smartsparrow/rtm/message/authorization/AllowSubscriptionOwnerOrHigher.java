package com.smartsparrow.rtm.message.authorization;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionMessage;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AllowSubscriptionOwnerOrHigher implements AuthorizationPredicate<SubscriptionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowSubscriptionOwnerOrHigher.class);

    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AllowSubscriptionOwnerOrHigher(SubscriptionPermissionService subscriptionPermissionService) {
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Allow account that have a permission level of owner or higher over the target subscription
     *
     * @param authenticationContext the context containing the account details
     * @param message the message received on the socket
     * @return <code>true</code> when the account has {@link PermissionLevel#OWNER} or higher over the target
     *                           subscription.
     *         <code>false</code> when the account is not found, or the account does not have
     *                           {@link PermissionLevel#OWNER} or higher over the target subscription
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, SubscriptionMessage message) {
        Account account = authenticationContext.getAccount();

        if (message.getSubscriptionId() != null) {
            return account != null && hasOwnerPermissionOrHigherOverSubscription(account.getId(),
                    message.getSubscriptionId());
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, `subscriptionId` was not supplied {}", message);
        }

        return false;
    }

    /**
     * Checks that the supplied account has owner or higher permission level over the supplied subscription.
     *
     * @param accountId the account id requesting the permission level
     * @param subscriptionId the targeting subscription id
     * @return <code>true</code> when the account has {@link PermissionLevel#OWNER} or higher over the subscription
     *         <code>false</code> when the account has no permission or the permission is lower than
     *         {@link PermissionLevel#OWNER}
     */
    private boolean hasOwnerPermissionOrHigherOverSubscription(UUID accountId, UUID subscriptionId) {
        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId)
                .block();

        if (permissionLevel == null) {
            return false;
        }

        return permissionLevel.isEqualOrHigherThan(PermissionLevel.OWNER);
    }
}
