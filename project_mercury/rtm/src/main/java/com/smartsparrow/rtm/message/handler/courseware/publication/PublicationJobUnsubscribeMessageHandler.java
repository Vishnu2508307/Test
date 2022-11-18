package com.smartsparrow.rtm.message.handler.courseware.publication;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationJobGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription.PublicationJobRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class PublicationJobUnsubscribeMessageHandler implements MessageHandler<PublicationJobGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationJobUnsubscribeMessageHandler.class);

    public static final String PUBLICATION_JOB_UNSUBSCRIBE = "publication.job.unsubscribe";
    private static final String PUBLICATION_JOB_UNSUBSCRIBE_OK = "publication.job.unsubscribe.ok";
    private static final String PUBLICATION_JOB_UNSUBSCRIBE_ERROR = "publication.job.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final PublicationJobRTMSubscriptionFactory publicationJobRTMSubscriptionFactory;

    @Inject
    public PublicationJobUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                   PublicationJobRTMSubscriptionFactory publicationJobRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.publicationJobRTMSubscriptionFactory = publicationJobRTMSubscriptionFactory;
    }

    @Override
    public void validate(PublicationJobGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getPublicationId() != null, "publicationId is required");
    }

    @Override
    public void handle(Session session, PublicationJobGenericMessage message) throws WriteResponseException {

        try {
            PublicationJobRTMSubscription publicationJobRTMSubscription = publicationJobRTMSubscriptionFactory.create(message.getPublicationId());

            rtmSubscriptionManagerProvider.get().unsubscribe(publicationJobRTMSubscription.getName());

            Responses.writeReactive(session, new BasicResponseMessage(PUBLICATION_JOB_UNSUBSCRIBE_OK, message.getId()));
        } catch (Throwable t) {
            log.jsonDebug( t.getMessage(), new HashMap<String, Object>(){
                {put("publicationId", message.getPublicationId());}
            });
            ErrorMessage error = new ErrorMessage(PUBLICATION_JOB_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage("error unsubscribing from publication job subscription")
                    .setCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
            Responses.write(session, error);
        }
    }
}
