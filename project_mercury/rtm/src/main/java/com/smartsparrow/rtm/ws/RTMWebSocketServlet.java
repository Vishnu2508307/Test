package com.smartsparrow.rtm.ws;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Use a servlet to handle the WebSocket upgrade requests.
 * <p>
 * This servlet sets the creator which creates the WebSocket broker as needed.
 */
@Singleton
@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "No need to serialize the servlet")
public class RTMWebSocketServlet extends WebSocketServlet {

    private static final int KB = 1024;

    private final RTMWebSocketCreator rtmWebSocketCreator;

    @Inject
    RTMWebSocketServlet(RTMWebSocketCreator rtmWebSocketCreator) {
        this.rtmWebSocketCreator = rtmWebSocketCreator;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        // set a 1 hour socket timeout
        factory.getPolicy().setIdleTimeout(TimeUnit.HOURS.toMillis(1));

        // set the max text message size to 3MB to support large json payload
        factory.getPolicy().setMaxTextMessageSize(3072 * KB);

        // set the factory/creator of the web socket broker.
        factory.setCreator(rtmWebSocketCreator);
    }

}
