package com.smartsparrow.rtm.message.event;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Event publisher interface
 * @param <T> an object that holds data to broadcast in an event message
 */
public interface EventPublisher<T extends BroadcastMessage> {

    void publish(RTMClient rtmClient, T data);
}
