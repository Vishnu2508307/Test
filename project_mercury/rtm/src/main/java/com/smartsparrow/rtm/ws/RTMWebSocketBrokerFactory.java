package com.smartsparrow.rtm.ws;

public interface RTMWebSocketBrokerFactory {

    /**
     * Create a new instance of RTMWebSocketBroker with a given context
     *
     * @param rtmWebSocketContext the context for the broker
     * @return the rtm websocket broker created instance
     */
    RTMWebSocketBroker create(final RTMWebSocketContext rtmWebSocketContext);
}
