package com.smartsparrow.rtm.message.handler.courseware.publication;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationListMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;

public class ListPublicationRequestMessageHandler implements MessageHandler<PublicationListMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListPublicationRequestMessageHandler.class);

    public static final String PUBLICATION_LIST_REQUEST = "publication.list.request";
    public static final String PUBLICATION_LIST_REQUEST_OK = "publication.list.request.ok";
    public static final String PUBLICATION_LIST_REQUEST_ERROR = "publication.list.request.error";

    private final PublicationService publicationService;

    @Inject
    public ListPublicationRequestMessageHandler(final PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PUBLICATION_LIST_REQUEST)
    @Override
    public void handle(Session session, PublicationListMessage message) throws WriteResponseException {

        try {
            publicationService.fetchPublicationWithMeta().collectList()
                    .subscribe(publicationWithMetadata -> {
                        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                PUBLICATION_LIST_REQUEST_OK,
                                message.getId());
                        basicResponseMessage.addField("publications", publicationWithMetadata);
                        Responses.writeReactive(session, basicResponseMessage);
                    });
        }

        catch (Exception ex) {
            Responses.errorReactive(session, message.getId(), PUBLICATION_LIST_REQUEST_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error listing publications");
        }
    }
}
