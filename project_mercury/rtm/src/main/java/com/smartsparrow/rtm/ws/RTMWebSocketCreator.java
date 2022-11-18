package com.smartsparrow.rtm.ws;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

/**
 * A simple WebSocketHandler that registers a single WebSocket POJO that is created on every upgrade request.
 *
 * This is a "Singleton"/Factory which is used during the Servlet request.
 *
 * @see RTMWebSocketServlet
 */
public class RTMWebSocketCreator implements WebSocketCreator {

    private static final Logger log = LoggerFactory.getLogger(RTMWebSocketCreator.class);

    private final Provider<RTMWebSocketBrokerFactory> rtmWebSocketBrokerFactoryProvider;

    @Inject
    RTMWebSocketCreator(Provider<RTMWebSocketBrokerFactory> rtmWebSocketBrokerFactoryProvider) {
        this.rtmWebSocketBrokerFactoryProvider = rtmWebSocketBrokerFactoryProvider;
    }

    /**
     * Creates a WebSocket broker.
     *
     * @param req the request
     * @param resp the response
     * @return a WebSocket broker to manage the messages on the WebSocket
     */
    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        if (log.isDebugEnabled()) {
            log.debug("Upgrade request from {}", req.getRemoteAddress());
        }

        return rtmWebSocketBrokerFactoryProvider.get()
                // create the web socket with the proper context
                .create(RTMWebSocketContext.from(req.getRequestURI().getPath()));
    }

}
