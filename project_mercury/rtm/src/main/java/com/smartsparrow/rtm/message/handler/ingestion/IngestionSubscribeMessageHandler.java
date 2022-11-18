package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription.IngestionRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class IngestionSubscribeMessageHandler implements MessageHandler<IngestionGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IngestionSubscribeMessageHandler.class);

    public static final String PROJECT_INGEST_SUBSCRIBE = "project.ingest.subscribe";
    private static final String PROJECT_INGEST_SUBSCRIBE_OK = "project.ingest.subscribe.ok";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final IngestionRTMSubscription.IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory;

    @Inject
    public IngestionSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                            final IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.ingestionRTMSubscriptionFactory = ingestionRTMSubscriptionFactory;
    }

    @Override
    public void validate(IngestionGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "ingestionId is required");
    }

    @Override
    public void handle(Session session, IngestionGenericMessage message) throws WriteResponseException {

        IngestionRTMSubscription ingestionRTMSubscription = ingestionRTMSubscriptionFactory.create(message.getIngestionId());
        rtmSubscriptionManagerProvider.get().add(ingestionRTMSubscription)
                .subscribe(
                        listenerId -> {
                        },
                        this::subscriptionOnErrorHandler,
                        () -> {
                            log.jsonDebug("client subscribing to events ", new HashMap<String, Object>() {
                                {
                                    put("ingestionId", message.getIngestionId());
                                }
                            });

                            BasicResponseMessage response = new BasicResponseMessage(PROJECT_INGEST_SUBSCRIBE_OK,
                                                                                     message.getId());
                            response.addField("rtmSubscriptionId", ingestionRTMSubscription.getId());
                            Responses.writeReactive(session, response);
                        });

    }

}
