package com.smartsparrow.rtm.message.handler;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageClassification;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.TimeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.Subscription;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Mono;

public class TimeSubscribeMessageHandler implements MessageHandler<TimeMessage> {

    public static final String TIME_SUBSCRIBE = "time.subscribe";
    public static final String TIME_SUBSCRIBE_OK = "time.subscribe.ok";
    public static final String TIME_SUBSCRIBE_ERROR = "time.subscribe.error";

    public static final String TIME_TOPIC = "time";

    private final Provider<SubscriptionManager> subscriptionManagerProvider;
    private final static int DEFAULT_INTERVAL = 30;

    @Inject
    TimeSubscribeMessageHandler(Provider<SubscriptionManager> subscriptionManagerProvider) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
    }

    @Override
    public void handle(Session session, TimeMessage message) throws WriteResponseException {

        // use the interval from the message or the default.
        int intervalSeconds = DEFAULT_INTERVAL;
        if (message.getInterval() != null) {
            intervalSeconds = message.getInterval();
        }

        // create the subscription and reply.
        try {
            createSubscription(session, message.getId(), intervalSeconds);

            //
            BasicResponseMessage responseMessage = new BasicResponseMessage(TIME_SUBSCRIBE_OK, message.getId());
            Responses.write(session, responseMessage);
        } catch (SubscriptionLimitExceeded |SubscriptionAlreadyExists ex) {
            // reply with an error.
            BasicResponseMessage responseMessage = new BasicResponseMessage(TIME_SUBSCRIBE_ERROR, message.getId());
            responseMessage.addField("reason", ex.getMessage());
            Responses.write(session, responseMessage);
        }
    }

    void createSubscription(final Session session, final String replyId, final int intervalSec)
            throws SubscriptionLimitExceeded, SubscriptionAlreadyExists {
        //
        final SubscriptionManager subscriptionManager = subscriptionManagerProvider.get();

        subscriptionManager.add(new Subscription<TimeMessage>() {
            Thread worker;

            @Override
            public String getId() {
                return TIME_TOPIC;
            }

            @Override
            public String getName() {
                return TIME_TOPIC;
            }

            @Override
            public Mono<Integer> subscribe(RTMClient rtmClient) {
                Runnable r = () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        long now = System.currentTimeMillis();

                        com.smartsparrow.rtm.message.send.TimeMessage reply;
                        reply = new com.smartsparrow.rtm.message.send.TimeMessage(MessageClassification.BROADCAST)//
                                .setReplyTo(replyId) //
                                .setEpochMilli(now) //
                                .setRfc1123(DateFormat.asRFC1123(now));

                        try {
                            Responses.write(session, reply);
                        } catch (WriteResponseException e) {
                            unsubscribe(rtmClient);
                        }

                        try {
                            Thread.sleep(intervalSec * 1000);
                        } catch (InterruptedException e) {
                            // We were sleeping while interrupted, propagate it.
                            Thread.currentThread().interrupt();
                        }
                    }
                };

                worker = new Thread(r);
                worker.start();

                return Mono.empty();
            }

            @Override
            public void unsubscribe(RTMClient rtmClient) {
                worker.interrupt();
            }

            @Override
            public Class<TimeMessage> getMessageType() {
                return TimeMessage.class;
            }
        });
    }
}
