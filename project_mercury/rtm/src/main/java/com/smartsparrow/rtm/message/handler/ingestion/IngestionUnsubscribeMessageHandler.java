package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription.IngestionRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class IngestionUnsubscribeMessageHandler implements MessageHandler<IngestionGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IngestionUnsubscribeMessageHandler.class);

    public static final String PROJECT_INGEST_UNSUBSCRIBE = "project.ingest.unsubscribe";
    private static final String PROJECT_INGEST_UNSUBSCRIBE_OK = "project.ingest.unsubscribe.ok";
    private static final String PROJECT_INGEST_UNSUBSCRIBE_ERROR = "project.ingest.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory;

    @Inject
    public IngestionUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                              final IngestionRTMSubscription.IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.ingestionRTMSubscriptionFactory = ingestionRTMSubscriptionFactory;
    }

    @Override
    public void validate(IngestionGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "ingestionId is required");
    }

    @Override
    public void handle(Session session, IngestionGenericMessage message) throws WriteResponseException {
        try {
            IngestionRTMSubscription ingestionRTMSubscription = ingestionRTMSubscriptionFactory.create(message.getIngestionId());
            rtmSubscriptionManagerProvider.get().unsubscribe(ingestionRTMSubscription.getName());
            Responses.write(session, new BasicResponseMessage(PROJECT_INGEST_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.debug("subscription not found ", subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(PROJECT_INGEST_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for ingestion %s not found", message.getIngestionId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
