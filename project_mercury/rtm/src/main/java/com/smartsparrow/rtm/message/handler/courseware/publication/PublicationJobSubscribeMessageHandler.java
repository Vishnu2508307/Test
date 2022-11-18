package com.smartsparrow.rtm.message.handler.courseware.publication;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationJobGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription.PublicationJobRTMSubscriptionFactory;

public class PublicationJobSubscribeMessageHandler implements MessageHandler<PublicationJobGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationJobSubscribeMessageHandler.class);

    public static final String PUBLICATION_JOB_SUBSCRIBE = "publication.job.subscribe";
    private static final String PUBLICATION_JOB_SUBSCRIBE_OK = "publication.job.subscribe.ok";
    private static final String PUBLICATION_JOB_SUBSCRIBE_ERROR = "publication.job.subscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    private final PublicationJobRTMSubscriptionFactory publicationJobRTMSubscriptionFactory;

    @Inject
    public PublicationJobSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
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
        PublicationJobRTMSubscription publicationJobRTMSubscription = publicationJobRTMSubscriptionFactory.create(message.getPublicationId());

        rtmSubscriptionManagerProvider.get().add(publicationJobRTMSubscription)
                .subscribe(listenerId -> {
                    BasicResponseMessage response = new BasicResponseMessage(PUBLICATION_JOB_SUBSCRIBE_OK, message.getId());
                    response.addField("rtmSubscriptionId", publicationJobRTMSubscription.getId());
                    Responses.writeReactive(session, response);
                }, ex -> {
                    log.jsonError(ex.getMessage(), new HashMap<>(), ex);
                    Responses.errorReactive(session, message.getId(), PUBLICATION_JOB_SUBSCRIBE_ERROR, 400, ex.getMessage());
                });
    }
}
