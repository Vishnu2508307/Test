package com.smartsparrow.rtm.subscription;

public class SubscriptionNotFound extends Exception {

    private final static String MSG = "Subscription with name '%s' is not found";

    public SubscriptionNotFound(String name) {
        super(String.format(MSG, name));
    }
}
