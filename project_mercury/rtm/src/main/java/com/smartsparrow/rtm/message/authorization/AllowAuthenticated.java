package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.ReceivedMessage;

/**
 * Authorization predicate that allows authenticated users only.
 *
 */
public class AllowAuthenticated implements AuthorizationPredicate<ReceivedMessage> {

    /**
     * Allow authenticated users.
     *
     * @param authenticationContext used to determine if the caller is authenticated
     * @param receivedMessage ignored
     *
     * @return true if the user is authenticated.
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, ReceivedMessage receivedMessage) {
        return authenticationContext.getAccount() != null;
    }

    @Override
    public String getErrorMessage() {
        return "not authenticated";
    }
}
