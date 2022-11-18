package com.smartsparrow.rtm.subscription.export;

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

class ExportEventRTMSubscriptionTest {

    @InjectMocks
    private ExportEventRTMSubscription subscription;

    @Mock
    private ExportEventRTMSubscription.ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory;

    private static final UUID exportId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new ExportEventRTMSubscription(exportId);
        when(exportEventRTMSubscriptionFactory.create(exportId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(exportEventRTMSubscriptionFactory.create(exportId), subscription);
        assertEquals(ExportEventRTMSubscription.NAME(exportId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("author.export.broadcast", subscription.getBroadcastType());
        assertEquals(ExportEventRTMSubscription.class, subscription.getSubscriptionType());
    }

}
