package com.smartsparrow.publication.service;

import static com.smartsparrow.dataevent.RouteUri.PUBLICATION_JOB_EVENT;
import static com.smartsparrow.dataevent.RouteUri.PUBLICATION_OCULUS_STATUS_EVENT;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;

import com.smartsparrow.courseware.eventmessage.PublicationJobEventMessage;
import com.smartsparrow.courseware.eventmessage.PublicationOculusEventMessage;
import com.smartsparrow.publication.data.PublicationOculusData;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class PublicationBroker {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationBroker.class);

    private static final String PUBLISHED = "PUBLISHED";
    private static final String REV = "REV";
    private static final String IN_REVIEW = "IN REVIEW";
    private static final String PUBLISHED_LIVE = "PUBLISHED LIVE";

    private final CamelReactiveStreamsService camel;

    @Inject
    public PublicationBroker(final CamelReactiveStreamsService camel) {

        this.camel = camel;
    }

    /**
     * Broadcast to the publication job subscription when a transform/publication status received from SNS.
     * @param publicationJobEventMessage
     */
    public void broadcast(PublicationJobEventMessage publicationJobEventMessage) {
         emitEvent(publicationJobEventMessage).thenReturn(publicationJobEventMessage).block();
    }

    /**
     * Emit an {@link PublicationJobEventMessage} so that listening subscriptions can broadcast the content to the
     * subscribed clients
     * @param publicationJobEventMessage
     * @return an exchange publisher mono
     */
    private Mono<Publisher<Exchange>> emitEvent(PublicationJobEventMessage publicationJobEventMessage) {
        return Mono.just(publicationJobEventMessage)
                .map(event -> camel.toStream(PUBLICATION_JOB_EVENT, event));
    }

    /**
     * Fetch the publication status from oculus API
     * @param bookId
     * @return Mono of PublicationOculusData
     */
    public Mono<PublicationOculusData> getOculusStatus(String bookId) {
        PublicationOculusData publicationOculusData = new PublicationOculusData();
        Mono<PublicationOculusData> publicationOculusDataMono = Mono.just(new PublicationOculusEventMessage(bookId))
                .doOnEach(log.reactiveInfo("handling oculus status"))
                .map(event -> camel.toStream(PUBLICATION_OCULUS_STATUS_EVENT, event, PublicationOculusEventMessage.class))
                .doOnEach(log.reactiveErrorThrowable("error while fetching oculus status"))
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("oculus status handling completed"))
                .map(publicationOculusEventMessage -> {
                    if(publicationOculusEventMessage != null && publicationOculusEventMessage.getOculusStatus() != null) {
                        if (publicationOculusEventMessage.getOculusStatus().equals(PUBLISHED)) {
                            if(bookId.contains(REV)) {
                                publicationOculusData.setOculusStatus(IN_REVIEW);
                            } else {
                                publicationOculusData.setOculusStatus(PUBLISHED_LIVE);
                            }
                        } else {
                            publicationOculusData.setOculusStatus(publicationOculusEventMessage.getOculusStatus());
                        }
                        publicationOculusData.setOculusVersion(publicationOculusEventMessage.getOculusVersion());
                    }
                    publicationOculusData.setBookId(bookId);

                    return publicationOculusData;
                });
        return publicationOculusDataMono;
    }
}
