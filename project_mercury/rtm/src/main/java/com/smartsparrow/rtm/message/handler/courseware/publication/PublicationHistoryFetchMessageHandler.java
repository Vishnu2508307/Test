package com.smartsparrow.rtm.message.handler.courseware.publication;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationHistoryFetchMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class PublicationHistoryFetchMessageHandler implements MessageHandler<PublicationHistoryFetchMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationHistoryFetchMessageHandler.class);

    public static final String PUBLICATION_HISTORY_FETCH = "publication.history.fetch";
    public static final String PUBLICATION_HISTORY_FETCH_OK = "publication.history.fetch.ok";
    public static final String PUBLICATION_HISTORY_FETCH_ERROR = "publication.history.fetch.error";

    private final PublicationService publicationService;

    @Inject
    public PublicationHistoryFetchMessageHandler(final PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public void validate(PublicationHistoryFetchMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "missing activityId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PUBLICATION_HISTORY_FETCH)
    @Override
    public void handle(Session session, PublicationHistoryFetchMessage message) throws WriteResponseException {

        try {
            publicationService.fetchPublicationForActivity(message.getActivityId(), message.getOutputType())
                    .collectList()
                    .subscribe(publicationPayloads -> {
                        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                PUBLICATION_HISTORY_FETCH_OK,
                                message.getId());
                        basicResponseMessage.addField("publications", publicationPayloads);
                        Responses.writeReactive(session, basicResponseMessage);
                    });
        }
        catch (Exception ex) {
            Responses.errorReactive(session, message.getId(), PUBLICATION_HISTORY_FETCH_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching publications");
        }
    }
}
