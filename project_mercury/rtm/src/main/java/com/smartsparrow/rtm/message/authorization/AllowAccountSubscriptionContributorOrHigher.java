package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.iam.AccountMessage;

/**
 * Authorizer used to check that a user has permission over the subscription linked to the account id provided in the
 * message.
 */
public class AllowAccountSubscriptionContributorOrHigher implements AuthorizationPredicate<AccountMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowAccountSubscriptionContributorOrHigher.class);

    private final AccountService accountService;
    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AllowAccountSubscriptionContributorOrHigher(AccountService accountService,
                                                       SubscriptionPermissionService subscriptionPermissionService) {
        this.accountService = accountService;
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, AccountMessage message) {
        Account account = authenticationContext.getAccount();
        Account target = accountService.findById(message.getAccountId()).blockLast();
        if (target != null) {
            PermissionLevel permission = subscriptionPermissionService
                    .findHighestPermissionLevel(account.getId(), target.getSubscriptionId())
                    .block();

            if (permission == null) {
                return false;
            }

            return permission.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
        }

        if (log.isDebugEnabled()) {
            log.debug("could not verify subscription permission level over account {}. The account was not found",
                    message.getAccountId());
        }


        return false;
    }
}
