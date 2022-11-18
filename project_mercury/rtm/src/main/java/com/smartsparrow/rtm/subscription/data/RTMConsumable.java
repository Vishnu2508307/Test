package com.smartsparrow.rtm.subscription.data;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.pubsub.data.EventConsumable;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * Represents an RTM consumable object
 *
 * @param <T> the type of consumable content
 */
public interface RTMConsumable<T extends BroadcastMessage> extends EventConsumable<T> {

    /**
     * The RTM client that produced this consumable
     *
     * @return the rtm client that produced this consumable
     */
    RTMClientContext getRTMClientContext();
}
