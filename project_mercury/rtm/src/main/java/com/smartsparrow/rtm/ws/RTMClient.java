package com.smartsparrow.rtm.ws;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.message.RTMClientContext;
/**
 * A class to encapsulate webSocket connection information. Holds the {@link Session} and {@link RTMClientContext}
 * and exposes accessors for those fields.
 */
public class RTMClient {

    private final Session session;
    private final RTMClientContext rtmClientContext;

    /**
     * Package-private constructor to initialize the object with the session and the client context.
     * This should only be called in {@link RTMWebSocketHandler#initialise(String, Session)} method.
     * @param session the webSocket session
     * @param rtmClientContext the connection {@link RTMClientContext}
     */
    RTMClient(Session session, RTMClientContext rtmClientContext) {
        this.session = session;
        this.rtmClientContext = rtmClientContext;
    }

    public Session getSession() {
        return session;
    }

    public RTMClientContext getRtmClientContext() {
        return rtmClientContext;
    }
}
