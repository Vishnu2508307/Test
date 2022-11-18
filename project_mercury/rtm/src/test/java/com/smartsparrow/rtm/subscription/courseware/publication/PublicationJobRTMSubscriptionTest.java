package com.smartsparrow.rtm.subscription.courseware.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription.PublicationJobRTMSubscriptionFactory;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class PublicationJobRTMSubscriptionTest {

    @InjectMocks
    private PublicationJobRTMSubscription subscription;

    @Mock
    private PublicationJobRTMSubscriptionFactory publicationJobRTMSubscriptionFactory;

    private static final UUID publicationId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(publicationJobRTMSubscriptionFactory.create(publicationId)).thenReturn(subscription);
        subscription = new PublicationJobRTMSubscription(publicationId);
        when(publicationJobRTMSubscriptionFactory.create(publicationId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(PublicationJobRTMSubscription.NAME(publicationId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("publication.job.broadcast", subscription.getBroadcastType());
        assertEquals(PublicationJobRTMSubscription.class, subscription.getSubscriptionType());
    }

}