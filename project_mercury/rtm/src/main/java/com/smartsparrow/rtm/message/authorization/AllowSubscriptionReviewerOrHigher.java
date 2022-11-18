package com.smartsparrow.rtm.message.authorization;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionMessage;

public class AllowSubscriptionReviewerOrHigher implements AuthorizationPredicate<SubscriptionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowSubscriptionReviewerOrHigher.class);

    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AllowSubscriptionReviewerOrHigher(SubscriptionPermissionService subscriptionPermissionService) {
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Allow account that have a permission level of contributor or higher over the target subscription
     *
     * @param authenticationContext the context containing the account details
     * @param message the message received on the socket
     * @return <code>true</code> when the account has {@link PermissionLevel#REVIEWER} or higher over the target
     *                           subscription.
     *         <code>false</code> when the account is not found, or the account does not have
     *                           {@link PermissionLevel#REVIEWER} or higher over the target subscription
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, SubscriptionMessage message) {
        Account account = authenticationContext.getAccount();

        if (message.getSubscriptionId() != null) {
            return account != null && hasReviewerPermissionOrHigherOverSubscription(account.getId(),
                    message.getSubscriptionId());
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, `subscriptionId` was not supplied {}", message);
        }

        return false;
    }

    /**
     * Checks that the supplied account has contributor or higher permission level over the supplied subscription.
     *
     * @param accountId the account id requesting the permission level
     * @param subscriptionId the targeting subscription id
     * @return <code>true</code> when the account has {@link PermissionLevel#REVIEWER} or higher over the subscription
     *         <code>false</code> when the account has no permission or the permission is lower than
     *         {@link PermissionLevel#REVIEWER}
     */
    private boolean hasReviewerPermissionOrHigherOverSubscription(UUID accountId, UUID subscriptionId) {
        PermissionLevel permissionLevel = subscriptionPermissionService
                .findHighestPermissionLevel(accountId, subscriptionId)
                .block();

        if (permissionLevel == null) {
            return false;
        }

        return permissionLevel.isEqualOrHigherThan(PermissionLevel.REVIEWER);
    }
}
