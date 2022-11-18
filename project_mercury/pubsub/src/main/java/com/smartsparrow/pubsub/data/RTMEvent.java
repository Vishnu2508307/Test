package com.smartsparrow.pubsub.data;

/**
 * Represent an RTM event. Those are the events that are at the core of RTM subscriptions and message broadcasting
 */
public interface RTMEvent {

    /**
     * @return the RTM event name
     */
    String getName();

    /**
     * Check that another rtm event is equals to this rtm event. Method used for filtering out consumers by event
     *
     * @param rtmEvent the event to check the equality of this event agains
     * @return true when equals, false when not equals
     */
    Boolean equalsTo(final RTMEvent rtmEvent);

    /**
     * @return the RTM event legacy name
     */
    String getLegacyName();
}
