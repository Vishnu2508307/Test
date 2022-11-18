package com.smartsparrow.rtm.subscription;

public class SubscriptionAlreadyExists extends Exception {

    private final static String DEFAULT_MSG = "Subscription already exists";

    public SubscriptionAlreadyExists() {
        this(DEFAULT_MSG);
    }

    public SubscriptionAlreadyExists(String message) {
        super(message);
    }

}
