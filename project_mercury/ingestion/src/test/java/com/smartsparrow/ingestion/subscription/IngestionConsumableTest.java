package com.smartsparrow.ingestion.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.util.UUIDs;

class IngestionConsumableTest {

    private static final UUID ingestionId = UUIDs.timeBased();

    @Mock
    private IngestionStatus ingestionStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        IngestionBroadcastMessage message = new IngestionBroadcastMessage()
                .setIngestionId(ingestionId)
                .setIngestionStatus(ingestionStatus);
        IngestionConsumable consumable = new IngestionConsumable(message);

        assertEquals(new IngestionRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("project.ingest/%s", ingestionId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
