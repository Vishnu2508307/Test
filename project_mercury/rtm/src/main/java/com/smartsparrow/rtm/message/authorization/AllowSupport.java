package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.ReceivedMessage;

public class AllowSupport implements AuthorizationPredicate<ReceivedMessage> {

    @Override
    public String getErrorMessage() {
        return "Forbidden";
    }

    /**
     * Allows authenticated user with role {@link AccountRole#SUPPORT}
     *
     * @param authenticationContext the context containing the authenticated account
     * @param receivedMessage the message received on the web socket channel
     * @return <code>true</code> if the user is authenticated and has SUPPORT role
     *         <code>false</code> when the user is either not authenticated or does not have SUPPORT role
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, ReceivedMessage receivedMessage) {
        Account account = authenticationContext.getAccount();

        return account != null && account.getRoles().stream()
                .anyMatch(one -> one.equals(AccountRole.SUPPORT));
    }
}
