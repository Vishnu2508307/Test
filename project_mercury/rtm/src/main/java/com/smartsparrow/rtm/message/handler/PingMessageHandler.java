package com.smartsparrow.rtm.message.handler;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.PingMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class PingMessageHandler implements MessageHandler<PingMessage> {

    public static final String PING = "ping";
    private static final String PONG = "pong";

    private static final Logger log = LoggerFactory.getLogger(PingMessageHandler.class);

    @Inject
    public PingMessageHandler() {
    }

    @Override
    public void handle(Session session, PingMessage message) throws WriteResponseException {
        if (log.isDebugEnabled()) {
            log.debug("handling: {}:{}", message.getType(), Strings.nullToEmpty(message.getId()));
        }

        BasicResponseMessage response = new BasicResponseMessage(PONG, message.getId());
        Responses.write(session, response);
    }
}
