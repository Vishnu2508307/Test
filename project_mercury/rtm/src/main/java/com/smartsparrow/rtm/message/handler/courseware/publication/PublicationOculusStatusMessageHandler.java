package com.smartsparrow.rtm.message.handler.courseware.publication;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.publication.data.PublicationOculusData;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationOculusStatusMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * This Handler is used to get the course publication status from oculus.
 */
public class PublicationOculusStatusMessageHandler implements MessageHandler<PublicationOculusStatusMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationOculusStatusMessageHandler.class);

    public static final String PUBLICATION_OCULUS_STATUS = "publication.oculus.status";
    private static final String PUBLICATION_OCULUS_STATUS_OK = "publication.oculus.status.ok";
    private static final String PUBLICATION_OCULUS_STATUS_ERROR = "publication.oculus.status.error";

    private final PublicationService publicationService;

    @Inject
    public PublicationOculusStatusMessageHandler(final PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public void validate(PublicationOculusStatusMessage message) throws RTMValidationException {
        affirmArgument(message.getBookId() != null, "bookId is required");
    }

    @Override
    public void handle(Session session, PublicationOculusStatusMessage message) throws WriteResponseException {

        try {
            PublicationOculusData publicationOculusData = publicationService.getOculusStatus(message.getBookId());
            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                    PUBLICATION_OCULUS_STATUS_OK,
                    message.getId());
            basicResponseMessage.addField("oculusData", publicationOculusData);
            Responses.writeReactive(session, basicResponseMessage);
        } catch (Exception ex) {
            Responses.errorReactive(session, message.getId(), PUBLICATION_OCULUS_STATUS_ERROR,
                                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching oculus status");
        }
    }
}
