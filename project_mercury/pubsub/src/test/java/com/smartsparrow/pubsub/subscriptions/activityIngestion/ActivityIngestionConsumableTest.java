package com.smartsparrow.pubsub.subscriptions.activityIngestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

public class ActivityIngestionConsumableTest {

    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final Object ingestionStatus = "UPLOADING";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void activityIngestion() {
        ActivityIngestionBroadcastMessage message = new ActivityIngestionBroadcastMessage(projectId,
                                                                                          ingestionId,
                                                                                          rootElementId,
                                                                                          ingestionStatus);
        ActivityIngestionConsumable consumable = new ActivityIngestionConsumable(message);

        assertEquals(new ActivityIngestionRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("author.activity/%s", rootElementId),
                            consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
