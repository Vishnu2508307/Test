package com.smartsparrow.rtm.message.handler.courseware.publication;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationHistoryDeleteMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class PublicationHistoryDeleteMessageHandler implements MessageHandler<PublicationHistoryDeleteMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationHistoryDeleteMessageHandler.class);

    public static final String PUBLICATION_HISTORY_DELETE = "publication.history.delete";
    public static final String PUBLICATION_HISTORY_DELETE_OK = "publication.history.delete.ok";
    public static final String PUBLICATION_HISTORY_DELETE_ERROR = "publication.history.delete.error";

    private final PublicationService publicationService;

    @Inject
    public PublicationHistoryDeleteMessageHandler(final PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public void validate(PublicationHistoryDeleteMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getPublicationId() != null, "missing publicationId");
            checkArgument(message.getActivityId() != null, "missing activityId");
            checkArgument(message.getVersion() != null, "missing version");
        } catch (IllegalArgumentException | ActivityNotFoundException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), PUBLICATION_HISTORY_DELETE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PUBLICATION_HISTORY_DELETE)
    @Override
    public void handle(Session session, PublicationHistoryDeleteMessage message) throws WriteResponseException {

        try {
            publicationService.deletePublicationHistory(message.getPublicationId(), message.getActivityId(), message.getVersion())
                    // link each signal to the current transaction token
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    // expire the transaction token on completion
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    // create a reactive context that enables all supported reactive monitoring
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .subscribe(unused -> {
                        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                PUBLICATION_HISTORY_DELETE_OK,
                                message.getId());
                        Responses.writeReactive(session, basicResponseMessage);
                    },
                               ex -> emitError(session, message, ex),
                               () -> {
                                   emitSuccess(session, message);

                               });
        }
        catch (Exception ex) {
            Responses.errorReactive(session, message.getId(), PUBLICATION_HISTORY_DELETE_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting publications history");
        }
    }


    private void emitSuccess(Session session, PublicationHistoryDeleteMessage message) {
        Responses.writeReactive(session, new BasicResponseMessage(PUBLICATION_HISTORY_DELETE_OK, message.getId())
                .addField("publicationId", message.getPublicationId()));
    }

    private void emitError(Session session, PublicationHistoryDeleteMessage message, Throwable ex) {
        log.jsonDebug("unable to delete publication history ", new HashMap<String, Object>() {
            {
                put("publicationId", message.getPublicationId());
                put("error", ex.getStackTrace());
            }
        });
        Responses.errorReactive(session, message.getId(), PUBLICATION_HISTORY_DELETE_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting publication history");
    }
}
