package com.smartsparrow.rtm.subscription.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class IngestionRTMSubscriptionTest {

    @InjectMocks
    private IngestionRTMSubscription subscription;

    @Mock
    private IngestionRTMSubscription.IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory;

    private static final UUID ingestionId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new IngestionRTMSubscription(ingestionId);
        when(ingestionRTMSubscriptionFactory.create(ingestionId)).thenReturn(subscription);

    }

    @Test
    void create() {
        assertEquals(ingestionRTMSubscriptionFactory.create(ingestionId), subscription);
        assertEquals(IngestionRTMSubscription.NAME(ingestionId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("project.ingest.broadcast", subscription.getBroadcastType());
        assertEquals(IngestionRTMSubscription.class, subscription.getSubscriptionType());
    }

}
