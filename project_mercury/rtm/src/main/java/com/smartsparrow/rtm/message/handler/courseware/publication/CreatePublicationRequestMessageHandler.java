package com.smartsparrow.rtm.message.handler.courseware.publication;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.publication.data.PublicationOutputType;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.CreatePublicationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class CreatePublicationRequestMessageHandler implements MessageHandler<CreatePublicationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreatePublicationRequestMessageHandler.class);

    public static final String PUBLICATION_CREATE_REQUEST = "publication.create.request";
    public static final String PUBLICATION_CREATE_REQUEST_OK = "publication.create.request.ok";
    public static final String PUBLICATION_CREATE_REQUEST_ERROR = "publication.create.request.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final PublicationService publicationService;

    @Inject
    public CreatePublicationRequestMessageHandler(final Provider<AuthenticationContext> authenticationContextProvider,
                                                  final PublicationService publicationService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.publicationService = publicationService;
    }

    @Override
    public void validate(CreatePublicationMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "missing activityId");
        affirmArgument(message.getAccountId() != null, "missing accountId");
        affirmArgument(message.getPublicationTitle() != null, "missing publicationTitle");
        affirmArgument(message.getVersion() != null, "missing version");
        // todo uncomment line below after outputType implemented in Workspace (BRNT-11923)
        // affirmArgument(message.getOutputType() != null, "missing outputType");

        // todo remove null check below after outputType implemented in Workspace (BRNT-11923)
        if (message.getOutputType() == null || (message.getOutputType() != PublicationOutputType.BRONTE_CLASSES_ON_DEMAND
                && message.getOutputType() != PublicationOutputType.BRONTE_PEARSON_PLUS)) {
            affirmArgument(message.getAuthor() != null, "missing author");
            affirmArgument(message.getExportId() != null, "missing exportId");
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PUBLICATION_CREATE_REQUEST)
    @Override
    public void handle(Session session, CreatePublicationMessage message) throws WriteResponseException {
        publicationService.createPublication(message.getActivityId(),
                                             message.getAccountId(),
                                             message.getExportId(),
                                             message.getPublicationTitle(),
                                             message.getDescription(),
                                             message.getAuthor(),
                                             message.getVersion(),
                                             message.getConfig(),
                                             // todo remove null case below after outputType implemented in Workspace (BRNT-11923)
                                             (message.getOutputType() == null) ? PublicationOutputType.EPUB_ETEXT : message.getOutputType())
                .doOnEach(log.reactiveErrorThrowable("error creating the publication",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getActivityId());
                                                         }
                                                     }))
                .subscribe(publicationId -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       PUBLICATION_CREATE_REQUEST_OK,
                                       message.getId());
                               basicResponseMessage.addField("publicationId", publicationId);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       PUBLICATION_CREATE_REQUEST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Error creating publication");
                           }
                );
    }
}
