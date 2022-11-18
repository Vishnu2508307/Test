package com.smartsparrow.rtm.message.handler.courseware.publication;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.CreatePublicationMessage;
import com.smartsparrow.rtm.message.recv.courseware.publication.UpdatePublicationTitleMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class UpdatePublicationTitleRequestMessageHandler implements MessageHandler<UpdatePublicationTitleMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdatePublicationTitleRequestMessageHandler.class);

    public static final String PUBLICATION_UPDATE_TITLE_REQUEST = "publication.title.update.request";
    public static final String PUBLICATION_UPDATE_TITLE_REQUEST_OK = "publication.title.update.request.ok";
    public static final String PUBLICATION_UPDATE_TITLE_REQUEST_ERROR = "publication.title.update.request.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final PublicationService publicationService;

    @Inject
    public UpdatePublicationTitleRequestMessageHandler(final Provider<AuthenticationContext> authenticationContextProvider,
                                                       final PublicationService publicationService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.publicationService = publicationService;
    }

    @Override
    public void validate(UpdatePublicationTitleMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "missing activityId");
        affirmArgument(message.getTitle() != null, "missing title");
        affirmArgument(message.getVersion() != null, "missing version");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PUBLICATION_UPDATE_TITLE_REQUEST)
    @Override
    public void handle(Session session, UpdatePublicationTitleMessage message) throws WriteResponseException {
        publicationService.updateTitle(message.getActivityId(),
                                             message.getTitle(), message.getVersion())
                .doOnEach(log.reactiveErrorThrowable("error updating title of publication",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getActivityId());
                                                             put("title", message.getTitle());
                                                         }
                                                     }))
                .then(publicationService.fetchPublicationForActivity(message.getActivityId()).collectList())
                .subscribe(publicationPayload -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       PUBLICATION_UPDATE_TITLE_REQUEST_OK,
                                       message.getId());
                               basicResponseMessage.addField("publicationPayload", publicationPayload);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       PUBLICATION_UPDATE_TITLE_REQUEST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "error updating title of publication");
                           }
                );
    }
}
