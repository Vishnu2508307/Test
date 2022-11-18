package com.smartsparrow.rtm.message;

import java.util.function.BiPredicate;

import com.smartsparrow.iam.service.AuthenticationContext;

/**
 * Perform Authorization of received messages in a Predicate style.
 *
 * Notes:
 *  - The supplied parameters passed to {test(context, message)} are guaranteed to not be null.
 *  - Exceptions should be caught and handled, not propagated.
 */
public interface AuthorizationPredicate<T extends MessageType> extends BiPredicate<AuthenticationContext, T> {

    /**
     * @return the reason for the authorizer's failure
     */
    String getErrorMessage();
}
