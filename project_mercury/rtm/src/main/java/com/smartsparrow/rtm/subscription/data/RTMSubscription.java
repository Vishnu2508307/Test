package com.smartsparrow.rtm.subscription.data;

import java.util.UUID;

import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.publisher.Mono;

/**
 * Represents an RTM Subscription
 */
public interface RTMSubscription {

    /**
     * @return the subscription id
     */
    UUID getId();

    /**
     * @return the subscription name
     */
    String getName();

    /**
     * Describe the broadcast message type
     *
     * @return the broadcast message type
     */
    String getBroadcastType();

    /**
     * Subscribe an RTMClient to the subscription
     *
     * @param rtmClient the rtm client that is subscribing
     */
    Mono<Integer> subscribe(RTMClient rtmClient);

    /**
     * Unsubscribe an RTMClient from a subscription
     *
     * @param rtmClient the rtm client to unsubscribe
     */
    void unsubscribe(RTMClient rtmClient);
}
