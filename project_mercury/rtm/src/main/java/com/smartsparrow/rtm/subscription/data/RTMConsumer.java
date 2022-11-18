package com.smartsparrow.rtm.subscription.data;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.pubsub.data.EventConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * This class represents a consumable object in RTM land.
 *
 * @param <T> an object that extends an AbstractRTMConsumable
 */
public interface RTMConsumer<T extends EventConsumable<? extends BroadcastMessage>> {

    /**
     * @return get the RTM event this consumer is for
     */
    RTMEvent getRTMEvent();

    /**
     * Consumer implementation that accepts an rtm client and an rtm consumable
     *
     * @param rtmClient the rtm client for this consumer
     * @param t the type of consumable consumed by this consumer
     */
    void accept(RTMClient rtmClient, T t);
}
