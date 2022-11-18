package com.smartsparrow.rtm.subscription;

/**
 * An exception to denote that the subscription limit has been reached.
 */
public class SubscriptionLimitExceeded extends Exception {

    private final static String DEFAULT_MSG = "Maximum number of subscriptions reached";

    public SubscriptionLimitExceeded() {
        this(DEFAULT_MSG);
    }

    public SubscriptionLimitExceeded(String message) {
        super(message);
    }

}
