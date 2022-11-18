package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.message.handler.TimeSubscribeMessageHandler.TIME_TOPIC;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.TimeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.util.Responses;

public class TimeUnsubscribeMessageHandler implements MessageHandler<TimeMessage> {

    public static final String TIME_UNSUBSCRIBE = "time.unsubscribe";
    public static final String TIME_UNSUBSCRIBE_OK = "time.unsubscribe.ok";
    public static final String TIME_UNSUBSCRIBE_ERROR = "time.unsubscribe.error";

    private final Provider<SubscriptionManager> subscriptionManagerProvider;

    @Inject
    TimeUnsubscribeMessageHandler(Provider<SubscriptionManager> subscriptionManagerProvider) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
    }

    @Override
    public void handle(Session session, TimeMessage message) throws WriteResponseException {

        SubscriptionManager subscriptionManager = subscriptionManagerProvider.get();

        try {
            subscriptionManager.unsubscribe(TIME_TOPIC);

            BasicResponseMessage responseMessage = new BasicResponseMessage(TIME_UNSUBSCRIBE_OK, message.getId());
            Responses.write(session, responseMessage);
        } catch (SubscriptionNotFound e) {
            BasicResponseMessage responseMessage = new BasicResponseMessage(TIME_UNSUBSCRIBE_ERROR, message.getId());
            responseMessage.addField("reason", e.getMessage());
            Responses.write(session, responseMessage);
        }
    }

}
