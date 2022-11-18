package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.DelayedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class DelayedMessageHandler implements MessageHandler<DelayedMessage> {

    public static final String DELAY = "delay";
    private static final String DELAY_OK = "delay.ok";
    private static final String DELAY_ERROR = "delay.error";

    @Inject
    public DelayedMessageHandler() {
    }

    @Override
    public void validate(DelayedMessage message) throws RTMValidationException {
        affirmArgument(message.getDelay() != null, "delay is required");
        affirmArgument(!(message.getDelay() > 5000), "max delay allowed is 5000");
        affirmArgument(!(message.getDelay() < 0), "negative delay value is not allowed");
    }

    /**
     * This is a very powerful message where someone can sabotage its own connection by sending a lot of these messages.
     * Its sole purpose of exist is ensuring that the {@link ReceivedMessage.Mode#WAIT_PENDING} works as expected.
     */
    @Override
    public void handle(Session session, DelayedMessage message) throws WriteResponseException {
        try {
            Thread.sleep(message.getDelay());
        } catch (InterruptedException e) {
            Responses.error(session, message.getId(), DELAY_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY, "interrupted exception");
        } finally {
            Responses.write(session, new BasicResponseMessage(DELAY_OK, message.getId()));
        }
    }
}
