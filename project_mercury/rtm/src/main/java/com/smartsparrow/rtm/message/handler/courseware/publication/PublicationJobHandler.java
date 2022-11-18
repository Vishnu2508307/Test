package com.smartsparrow.rtm.message.handler.courseware.publication;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.smartsparrow.courseware.eventmessage.PublicationJobEventMessage;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class PublicationJobHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationJobHandler.class);

    public static final String PUBLICATION_JOB_EVENT_MESSAGE = "publicationJobEventMessage";

    private final PublicationJobRTMProducer publicationJobRTMProducer;

    @Inject
    public PublicationJobHandler(final PublicationJobRTMProducer publicationJobRTMProducer) {
        this.publicationJobRTMProducer = publicationJobRTMProducer;
    }

    @Handler
    public void handle(Exchange exchange) throws Exception {
        log.info(" In handle() method");
        PublicationJobEventMessage event = exchange.getProperty(PUBLICATION_JOB_EVENT_MESSAGE, PublicationJobEventMessage.class);

        publicationJobRTMProducer.buildPublicationJobRTMConsumable(null, event.getContent().getPublicationId(),
                                                                   event.getContent().getPublicationJobStatus(),
                                                                   event.getContent().getJobId(), event.getContent().getStatusMessage(),
                                                                   event.getContent().getBookId(), event.getContent().getEtextVersion()).produce();
    }
}
