package com.smartsparrow.rtm.wiring;

import com.google.inject.servlet.ServletModule;
import com.smartsparrow.rtm.ws.RTMWebSocketBrokerFactory;
import com.smartsparrow.rtm.ws.RTMWebSocketBrokerFactoryImpl;
import com.smartsparrow.rtm.ws.RTMWebSocketContext;
import com.smartsparrow.rtm.ws.RTMWebSocketServlet;

/**
 * Servlet module which maps the WebSocket handler into the running server context.
 *
 */
class RTMWebSocketModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(RTMWebSocketBrokerFactory.class).to(RTMWebSocketBrokerFactoryImpl.class);

        serve(RTMWebSocketContext.SOCKET.getPath()).with(RTMWebSocketServlet.class);
        serve(RTMWebSocketContext.LEARN.getPath()).with(RTMWebSocketServlet.class);
    }

}
