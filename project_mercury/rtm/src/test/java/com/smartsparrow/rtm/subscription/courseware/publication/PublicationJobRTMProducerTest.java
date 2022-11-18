package com.smartsparrow.rtm.subscription.courseware.publication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class PublicationJobRTMProducerTest {

    @InjectMocks
    private PublicationJobRTMProducer publicationJobRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID publicationId = UUIDs.timeBased();
    private static final UUID jobId = UUIDs.timeBased();
    private static final PublicationJobStatus status = PublicationJobStatus.STARTED;
    private static final String statusMessage = "Transform failed";
    private static final String bookId = "BRNT-BJA0BXG95CK";
    private static final String etextVersion = "1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        publicationJobRTMProducer.buildPublicationJobRTMConsumable(rtmClientContext, publicationId, status, jobId, statusMessage,
                                                                   bookId, etextVersion);
        assertNotNull(publicationJobRTMProducer.getEventConsumable());
    }

}
