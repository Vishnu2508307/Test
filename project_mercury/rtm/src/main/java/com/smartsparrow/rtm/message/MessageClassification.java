package com.smartsparrow.rtm.message;

public enum MessageClassification {

    OK("ok"),
    ERROR("error"),
    SUBSCRIBE("subscribe"),
    BROADCAST("broadcast"),
    UNSUBSCRIBE("unsubscribe");

    private final String behaviour;

    MessageClassification(final String behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public String toString() {
        return behaviour;
    }
}
