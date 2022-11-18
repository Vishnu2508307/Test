package com.smartsparrow.rtm.subscription.courseware.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class PublicationJobRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID publicationId = UUIDs.timeBased();
    private static final UUID jobId = UUIDs.timeBased();
    private static final String statusMessage = "Transform failed";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        PublicationJobBroadcastMessage message = new PublicationJobBroadcastMessage(publicationId, PublicationJobStatus.STARTED,
                                                                                    jobId, statusMessage, "BRNT-BJA0BXG95CK", "1");
        PublicationJobRTMConsumable consumable = new PublicationJobRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new PublicationRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(PublicationJobRTMSubscription.NAME(publicationId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}