package com.smartsparrow.cache.diffsync;

/**
 * Class responsible for producing an event that will be picked up by a subscription topic and broadcast
 */
public interface DiffSyncEventProducer {

    /**
     * Produce an event Consumable event targeting a specific subscription topic
     */
    void produce();
}
