package com.smartsparrow.rtm.ws;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A Manager that keeps track of all the opened socket connections
 */
public class RTMWebSocketManager {

    // singleton instance
    private static final RTMWebSocketManager INSTANCE = new RTMWebSocketManager();

    public static RTMWebSocketManager getInstance() {
        return INSTANCE;
    }

    private final List<RTMWebSocketBroker> connections = new CopyOnWriteArrayList<>();

    private boolean _shutdownState = false;

    /**
     * Registers a new connection to the list.
     *
     * @param rtmWebSocketBroker the broker connection instance to add
     */
    void register(RTMWebSocketBroker rtmWebSocketBroker) {
        connections.add(rtmWebSocketBroker);
    }

    /**
     * Unregisters a connection from the list.
     *
     * @param rtmWebSocketBroker the broker connection instance to remove
     */
    void deregister(RTMWebSocketBroker rtmWebSocketBroker) {
        connections.remove(rtmWebSocketBroker);
    }

    /**
     * Triggers a graceful shutdown of all the opened connections.
     *
     * @param timeout the timeout value for the graceful shutdown
     * @param timeUnit the timeout value's timeUnit
     */
    public void closeAllConnections(final long timeout, final TimeUnit timeUnit) {
        _shutdownState = true;
        for (RTMWebSocketBroker connection : connections) {
            connection.gracefulShutdown(timeout, timeUnit);
        }
    }

    /**
     * A convenience method to check if all the connections have been closed
     *
     * @return {@code true} if there are connections in the list
     *         {@code false} if the list has no connections
     */
    public boolean hasConnections() {
        return connections.size() > 0;
    }

    /**
     * Returns the shutdown state value.
     *
     * @return {@code true} if the server is about to shutdown
     *         {@code false} if the shutdown value false
     */
    boolean isShuttingDown() {
        return _shutdownState;
    }
}
