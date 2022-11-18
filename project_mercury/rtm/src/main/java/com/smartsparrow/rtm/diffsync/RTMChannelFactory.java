package com.smartsparrow.rtm.diffsync;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.api.Session;

/**
 * This class is responsible for building an RTMChannel
 */
@Singleton
public class RTMChannelFactory {

    // dependency injected
    private final MessageTypeBridgeConverter converter;

    @Inject
    public RTMChannelFactory(MessageTypeBridgeConverter converter) {
        this.converter = converter;
    }

    public RTMChannel create(final Session session, final String clientId) {
        return new RTMChannel(session, clientId, converter);
    }
}
