package com.smartsparrow.publication.service;

import static com.smartsparrow.dataevent.RouteUri.PUBLICATION_JOB_EVENT;
import static com.smartsparrow.dataevent.RouteUri.PUBLICATION_OCULUS_STATUS_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.courseware.eventmessage.PublicationJobEventMessage;
import com.smartsparrow.courseware.eventmessage.PublicationOculusEventMessage;
import com.smartsparrow.publication.data.PublicationOculusData;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class PublicationBrokerTest {

    private static final String PUBLISHED = "PUBLISHED";
    private static final String BOOK_ID = "BRNT-BJA0BXG95CK";
    private static final String IN_REVIEW = "IN REVIEW";
    private static final String VERSION = "v3";

    @InjectMocks
    private PublicationBroker publicationBroker;

    @Mock
    private CamelReactiveStreamsService camel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        TestPublisher<Exchange> exchangeTestPublisher = TestPublisher.create();
        when(camel.toStream(eq(PUBLICATION_JOB_EVENT), any(PublicationJobEventMessage.class)))
                .thenReturn(exchangeTestPublisher);

        PublicationOculusEventMessage publicationOculusEventMessage = new PublicationOculusEventMessage(BOOK_ID);
        publicationOculusEventMessage.setOculusStatus(PUBLISHED);
        publicationOculusEventMessage.setOculusVersion(VERSION);
        when(camel.toStream(eq(PUBLICATION_OCULUS_STATUS_EVENT), any(PublicationOculusEventMessage.class), eq(PublicationOculusEventMessage.class)))
                .thenReturn(Mono.just(publicationOculusEventMessage));

    }

    @Test
    void broadcast_completeSuccess() {
        PublicationJobEventMessage publicationJobEventMessage = new PublicationJobEventMessage();
       publicationJobEventMessage.setContent( new PublicationJobBroadcastMessage(UUIDs.timeBased(),
                                                                                 PublicationJobStatus.COMPLETED,
                                                                                 UUIDs.timeBased(), "Publish successful",
                                                                                 BOOK_ID,
                                                                                 "1"));
        publicationBroker.broadcast(publicationJobEventMessage);
        verify(camel).toStream(eq(PUBLICATION_JOB_EVENT), any(PublicationJobEventMessage.class));
    }

    @Test
    void getOculusStatus() {
        PublicationOculusData publicationOculusData = publicationBroker.getOculusStatus(BOOK_ID+"-REV").block();
        assertNotNull(publicationOculusData);
        assertEquals(IN_REVIEW, publicationOculusData.getOculusStatus());
        assertEquals(VERSION, publicationOculusData.getOculusVersion());
    }

}
