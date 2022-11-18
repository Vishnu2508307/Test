package com.smartsparrow.rtm.message.handler;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageClassification;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.send.TimeMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.DateFormat;

public class TimeMessageHandler implements MessageHandler<ReceivedMessage> {

    public static final String TIME = "time";

    @Override
    public void handle(Session session, ReceivedMessage message) throws WriteResponseException {
        long now = System.currentTimeMillis();

        TimeMessage reply = new TimeMessage(MessageClassification.OK) //
                .setReplyTo(message.getId()) //
                .setEpochMilli(now) //
                .setRfc1123(DateFormat.asRFC1123(now));

        Responses.write(session, reply);
    }
}
