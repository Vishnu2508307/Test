package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.ReceivedMessage;

/**
 * Authorization Predicate that allows everyone to perform the operation.
 */
public class Everyone implements AuthorizationPredicate<ReceivedMessage> {

    /**
     * Always return true
     *
     * @param authenticationContext ignored
     * @param receivedMessage ignored
     * @return true, always.
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, ReceivedMessage receivedMessage) {
        return true;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
