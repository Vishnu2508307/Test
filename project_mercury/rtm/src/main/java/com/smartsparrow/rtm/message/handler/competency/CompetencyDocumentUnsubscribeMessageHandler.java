package com.smartsparrow.rtm.message.handler.competency;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.competency.CompetencyDocumentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription.CompetencyDocumentEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class CompetencyDocumentUnsubscribeMessageHandler implements MessageHandler<CompetencyDocumentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CompetencyDocumentUnsubscribeMessageHandler.class);

    public static final String COMPETENCY_DOCUMENT_UNSUBSCRIBE = "workspace.competency.document.unsubscribe";
    private static final String COMPETENCY_DOCUMENT_UNSUBSCRIBE_OK = "workspace.competency.document.unsubscribe.ok";
    private static final String COMPETENCY_DOCUMENT_UNSUBSCRIBE_ERROR = "workspace.competency.document.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final CompetencyDocumentEventRTMSubscriptionFactory competencyDocumentEventRTMSubscriptionFactory;

    @Inject
    public CompetencyDocumentUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
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
        try {
            log.jsonInfo("un-subscribing from changes on competency document", new HashMap<String, Object>(){
                {
                    put("documentId", message.getDocumentId());
                }
            });
            CompetencyDocumentEventRTMSubscription competencyDocumentEventRTMSubscription =
                    competencyDocumentEventRTMSubscriptionFactory.create(message.getDocumentId());
            rtmSubscriptionManagerProvider.get().unsubscribe(competencyDocumentEventRTMSubscription.getName());
            Responses.writeReactive(session, new BasicResponseMessage(COMPETENCY_DOCUMENT_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound e) {
            Responses.errorReactive(session, message.getId(), COMPETENCY_DOCUMENT_UNSUBSCRIBE_ERROR,
                    HttpStatus.SC_NOT_FOUND, e.getMessage());
        }
    }
}
