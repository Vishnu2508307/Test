package com.smartsparrow.pubsub.data;

/**
 * Class responsible for producing an event that will be picked up by a subscription topic and broadcast
 */
public interface EventProducer {

    /**
     * Produce an event Consumable event targeting a specific subscription topic
     */
    void produce();
}
