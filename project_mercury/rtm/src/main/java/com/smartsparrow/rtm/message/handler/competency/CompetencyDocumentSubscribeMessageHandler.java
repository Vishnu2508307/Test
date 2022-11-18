package com.smartsparrow.rtm.message.handler.competency;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.competency.CompetencyDocumentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription.CompetencyDocumentEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class CompetencyDocumentSubscribeMessageHandler implements MessageHandler<CompetencyDocumentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CompetencyDocumentSubscribeMessageHandler.class);

    public static final String COMPETENCY_DOCUMENT_SUBSCRIBE = "workspace.competency.document.subscribe";
    private static final String COMPETENCY_DOCUMENT_SUBSCRIBE_OK = "workspace.competency.document.subscribe.ok";
    public static final String COMPETENCY_DOCUMENT_BROADCAST = "workspace.competency.document.broadcast";
    public static final String COMPETENCY_DOCUMENT_BROADCAST_ERROR = "workspace.competency.document.broadcast.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final CompetencyDocumentEventRTMSubscriptionFactory competencyDocumentEventRTMSubscriptionFactory;

    @Inject
    public CompetencyDocumentSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                     CompetencyDocumentEventRTMSubscriptionFactory competencyDocumentEventRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.competencyDocumentEventRTMSubscriptionFactory = competencyDocumentEventRTMSubscriptionFactory;
    }

    @Override
    public void validate(CompetencyDocumentMessage message) throws RTMValidationException {
        affirmArgument(message.getDocumentId() != null, "documentId is required");
    }

    @Override
    public void handle(Session session, CompetencyDocumentMessage message) throws WriteResponseException {

        CompetencyDocumentEventRTMSubscription competencyDocumentEventRTMSubscription =
                competencyDocumentEventRTMSubscriptionFactory.create(message.getDocumentId());
        log.jsonInfo("client subscribing to events on competency document", new HashMap<String, Object>(){
            {
                put("documentId", message.getDocumentId());
            }
        });

        rtmSubscriptionManagerProvider.get().add(competencyDocumentEventRTMSubscription)
                .subscribe(listenerId -> {},
                           this::subscriptionOnErrorHandler,
                           () -> Responses.writeReactive(session, new BasicResponseMessage(COMPETENCY_DOCUMENT_SUBSCRIBE_OK, message.getId())
                                   .addField("rtmSubscriptionId", competencyDocumentEventRTMSubscription.getId()))
                );
    }
}
