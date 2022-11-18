package com.smartsparrow.pubsub.data;

import java.util.UUID;

import com.smartsparrow.data.Consumable;
import com.smartsparrow.dataevent.BroadcastMessage;

/**
 * Represents an event consumable object
 *
 * @param <T> the type of consumable content
 */
public interface EventConsumable<T extends BroadcastMessage> extends Consumable<T> {

    /**
     * Describes the name of this consumable
     *
     * @return the name of this consumable
     */
    String getName();

    /**
     * Describe the name of the subscription this consumer belongs to
     *
     * @return the subscription name
     */
    String getSubscriptionName();

    /**
     * @return the original subscription id the consumable is for
     */
    UUID getSubscriptionId();

    /**
     * @return the subscription broadcast message type to send on the websocket as a {com.smartsparrow.rtm.message.MessageType}
     */
    String getBroadcastType();

    /**
     * @return get the RTM event this consumer is for
     */
    RTMEvent getRTMEvent();
}
